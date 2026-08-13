import threading
import time
from datetime import datetime, timezone

from labflow.api.http_client import HttpClient
from labflow.system.collector import (
    SystemMetricCollector,
)


class SystemMetricSampler:

    def __init__(
        self,
        *,
        run_id: int,
        http_client: HttpClient,
        collector: SystemMetricCollector,
        started_at: float,
        interval_seconds: float = 1.0,
    ):
        self._run_id = run_id
        self._http_client = http_client
        self._collector = collector
        self._started_at = started_at
        self._interval_seconds = interval_seconds

        self._stop_event = threading.Event()

        self._thread: threading.Thread | None = None

    def start(self) -> None:

        if self._thread is not None:
            return

        self._thread = threading.Thread(
            target=self._run,
            name="labflow-system-metric-sampler",
            daemon=True,
        )

        self._thread.start()

    def stop(self) -> None:

        self._stop_event.set()

        if self._thread is not None:
            self._thread.join(
                timeout=self._interval_seconds + 1
            )

    def _run(self) -> None:

        while not self._stop_event.wait(
            self._interval_seconds
        ):
            try:
                self._send_snapshot()
            except Exception:
                # LabFlow 수집 실패가
                # 사용자 학습을 중단시키면 안 됨
                pass

    def _send_snapshot(self) -> None:

        metrics = self._collector.collect()

        payload = {
            "elapsedTime": (
                time.monotonic()
                - self._started_at
            ),
            "system": metrics.to_dict(),
            "recordedAt": (
                datetime.now(timezone.utc)
                .isoformat()
            ),
        }

        self._http_client.post_json(
            (
                f"/api/v1/sdk/runs/"
                f"{self._run_id}/system-metrics"
            ),
            payload,
        )