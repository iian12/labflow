import math
import time

from event import MetricEvent
from service import MetricService
from collector import SystemMetricCollector

class ActiveRun:
    def __init__(self, run_id: int, metric_service: MetricService, system_collector: SystemMetricCollector):
        self._run_id = run_id
        self._metric_service = metric_service
        self._system_collector = system_collector

        # elapsed_time 측정음
        self._started_at = time.monotonic()

        self._finished = False

    @property
    def id(self) -> int:
        return self._run_id

    def log_metrics(self,
                    *,
                    metrics: dict[str, object],
                    epoch: int | None = None,
                    step: int | None = None) -> None:

        # 종료된 Run에는 Metric을 기록하지 않음
        if self._finished:
            raise RuntimeError(
                "Cannot log metrics to a finished run"
            )

        metrics = metrics or {}

        self._validate(
            epoch=epoch,
            step=step,
            metrics=metrics,
        )

        # 사용자가 전달한 값을 문자열로 정규화
        normalized_metrics = self._normalized_metrics(metrics)

        # SDK 자동 수집 항목
        system_metrics = (
            self._system_collector.collect()
        )

        elapsed_time = (
            time.monotonic() - self._started_at
        )

        event = MetricEvent(
            epoch=epoch,
            step=step,
            elapsed_time=elapsed_time,
            metrics=normalized_metrics,
            system=system_metrics.to_dict(),
            recorded_at=MetricEvent.now_utc(),
        )

        self._metric_service.send(
            run_id=self._run_id,
            event=event,
        )

    def finish(self) -> None:
        """
        현재 Run을 종료 상태로 변경한다.

        실제 서버의 COMPLETED 처리 API가 따로 있다면
        이후 RunService 등을 연결하면 된다.
        """

        if self._finished:
            return
        self._finished = True

    @staticmethod
    def _validate(
            *,
            epoch: int,
            step: int,
            metrics: dict[str, object],
    ) -> None:

        if epoch is not None:
            if not isinstance(epoch, int):
                raise TypeError(
                    "epoch must be an integer or None"
                )

            if epoch < 0:
                raise ValueError(
                    "epoch must not be negative"
                )

        if step is not None:
            if not isinstance(step, int):
                raise TypeError(
                    "step must be an integer or None"
                )

            if step < 0:
                raise ValueError(
                    "step must not be negative"
                )

        if not isinstance(metrics, dict):
            raise TypeError(
                "metrics must be a dictionary"
            )

        # metrics={} 허용
        for key, value in metrics.items():

            if not isinstance(key, str):
                raise TypeError(
                    "metric key must be a string"
                )

            if not key.strip():
                raise ValueError(
                    "metric key must not be empty"
                )

            if value is None:
                raise ValueError(
                    f"metric '{key}' must not be None"
                )

            if isinstance(value, float):
                if not math.isfinite(value):
                    raise ValueError(
                        f"metric '{key}' must be finite"
                    )

    @staticmethod
    def _normalized_metrics(metrics: dict[str, object]) -> dict[str, str]:
        normalized: dict[str, str] = {}

        for key, value in metrics.items():
            normalized[key] = str(value)

        return normalized
