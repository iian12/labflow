from dataclasses import dataclass, asdict
from typing import Any

import psutil


@dataclass(frozen=True)
class GpuMetric:

    # GPU index
    index: int

    # GPU 사용률 (%)
    utilization: float

    # 사용 중인 VRAM (bytes)
    memory_used: int

    # 전체 VRAM (bytes)
    memory_total: int


@dataclass(frozen=True)
class SystemMetrics:

    # CPU 사용률 (%)
    cpu_utilization: float

    # RAM 사용량 (bytes)
    ram_used: int

    # 전체 RAM (bytes)
    ram_total: int

    # GPU별 사용량
    gpus: list[GpuMetric]

    def to_dict(self) -> dict[str, Any]:
        return {
            "cpuUtilization": self.cpu_utilization,
            "ramUsed": self.ram_used,
            "ramTotal": self.ram_total,
            "gpus": [
                asdict(gpu)
                for gpu in self.gpus
            ],
        }


class SystemMetricCollector:

    def collect(self) -> SystemMetrics:

        memory = psutil.virtual_memory()

        return SystemMetrics(
            cpu_utilization=psutil.cpu_percent(
                interval=None
            ),
            ram_used=memory.used,
            ram_total=memory.total,
            gpus=self._collect_gpus(),
        )

    @staticmethod
    def _collect_gpus() -> list[GpuMetric]:

        try:
            import pynvml

            pynvml.nvmlInit()

        except Exception:
            return []

        metrics: list[GpuMetric] = []

        try:
            count = pynvml.nvmlDeviceGetCount()

            for index in range(count):
                handle = (
                    pynvml.nvmlDeviceGetHandleByIndex(
                        index
                    )
                )

                utilization = (
                    pynvml.nvmlDeviceGetUtilizationRates(
                        handle
                    )
                )

                memory = (
                    pynvml.nvmlDeviceGetMemoryInfo(
                        handle
                    )
                )

                metrics.append(
                    GpuMetric(
                        index=index,
                        utilization=float(
                            utilization.gpu
                        ),
                        memory_used=int(
                            memory.used
                        ),
                        memory_total=int(
                            memory.total
                        ),
                    )
                )

        except Exception:
            return []

        finally:
            try:
                pynvml.nvmlShutdown()
            except Exception:
                pass

        return metrics