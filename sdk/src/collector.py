from dataclasses import dataclass, asdict

import psutil

@dataclass(frozen=True)
class SystemMetrics:
    # CPU 사용률(%)
    cpu_utilization: float

    # 현재 프로세스/시스템에서 사용 중인 RAM (bytes)
    ram_used: int

    # GPU 사용률 (%)
    gpu_utilization: float | None = None

    # 사용 중인 VRAM (bytes)
    gpu_memory_used: int | None = None

    def to_dict(self) -> dict:
        return {
            key: value
            for key, value in asdict(self).items()
            if value is not None
        }

class SystemMetricCollector:

    def collect(self) -> SystemMetrics:
        cpu_utilization = psutil.cpu_percent(interval=None)

        memory = psutil.virtual_memory()

        gpu_utilization = None
        gpu_memory_used = None

        try:
            gpu_utilization, gpu_memory_used = (self._collect_nvidia_gpu())
        except:
            pass

        return SystemMetrics(
            cpu_utilization=cpu_utilization,
            ram_used=memory.used,
            gpu_utilization=gpu_utilization,
            gpu_memory_used=gpu_memory_used,
        )

    @staticmethod
    def _collect_nvidia_gpu() -> tuple[float, int]:
        import pynvml

        pynvml.nvmlInit()

        try:
            handle = pynvml.nvmlDeviceGetHandleByIndex(0)

            utilization = (
                pynvml.nvmlDeviceGetUrilizationRates(handle)
            )

            memory = (
                pynvml.nvmlDeviceGetMemoryInfo(handle)
            )

            return (
                float(utilization.gpu),
                int(memory.used),
            )

        finally:
            pynvml.nvmlShutdown()