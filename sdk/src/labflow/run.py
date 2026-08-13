import json
import math
import sys
import time
from pathlib import Path

from labflow.client import HttpClient
from labflow.logging.stream import LabFlowStream
from labflow.metrics.event import MetricEvent
from labflow.system.collector import (
    SystemMetricCollector,
)
from labflow.system.sampler import (
    SystemMetricSampler,
)


class ActiveRun:

    def __init__(
        self,
        *,
        run_id: int,
        http_client: HttpClient,
        system_collector: SystemMetricCollector,
        system_metric_interval_seconds: float,
        capture_output: bool,
    ):
        self._run_id = run_id
        self._http_client = http_client
        self._system_collector = system_collector

        # elapsed_time 기준 시각
        self._started_at = time.monotonic()

        self._finished = False

        self._original_stdout = None
        self._original_stderr = None

        self._system_sampler = SystemMetricSampler(
            run_id=run_id,
            http_client=http_client,
            collector=system_collector,
            started_at=self._started_at,
            interval_seconds=(
                system_metric_interval_seconds
            ),
        )

        self._system_sampler.start()

        if capture_output:
            self._install_output_capture()

    @property
    def id(self) -> int:
        return self._run_id

    # -----------------------------------------
    # Parameters
    # -----------------------------------------

    def log_params(
        self,
        params: dict[str, object],
    ) -> None:

        self._ensure_active()

        normalized = self._normalize_map(
            params
        )

        self._http_client.post_json(
            (
                f"/api/v1/sdk/runs/"
                f"{self._run_id}/parameters"
            ),
            {
                "parameters": normalized
            },
        )

    # -----------------------------------------
    # Metrics
    # -----------------------------------------

    def log_metrics(
        self,
        *,
        metrics: dict[str, object] | None = None,
        epoch: int | None = None,
        step: int | None = None,
    ) -> None:

        self._ensure_active()

        metrics = metrics or {}

        self._validate_metrics(
            epoch=epoch,
            step=step,
            metrics=metrics,
        )

        normalized_metrics = (
            self._normalize_map(metrics)
        )

        event = MetricEvent(
            epoch=epoch,
            step=step,
            elapsed_time=(
                time.monotonic()
                - self._started_at
            ),
            metrics=normalized_metrics,
            recorded_at=MetricEvent.now_utc(),
        )

        self._http_client.post_json(
            (
                f"/api/v1/sdk/runs/"
                f"{self._run_id}/metrics"
            ),
            event.to_payload(),
        )

    # -----------------------------------------
    # Artifact
    # -----------------------------------------

    def log_artifact(
        self,
        *,
        path: str | Path,
        artifact_type: str,
        name: str | None = None,
        epoch: int | None = None,
        step: int | None = None,
        metadata: dict[str, object] | None = None,
    ) -> None:

        self._ensure_active()

        artifact_path = Path(path)

        if not artifact_path.is_file():
            raise FileNotFoundError(
                f"Artifact not found: {artifact_path}"
            )

        if epoch is not None and epoch < 0:
            raise ValueError(
                "epoch must not be negative"
            )

        if step is not None and step < 0:
            raise ValueError(
                "step must not be negative"
            )

        normalized_metadata = (
            self._normalize_map(
                metadata or {}
            )
        )

        data = {
            "type": artifact_type,
            "name": (
                name
                or artifact_path.name
            ),
            "metadata": json.dumps(
                normalized_metadata
            ),
        }

        if epoch is not None:
            data["epoch"] = str(epoch)

        if step is not None:
            data["step"] = str(step)

        self._http_client.post_file(
            (
                f"/api/v1/sdk/runs/"
                f"{self._run_id}/artifacts"
            ),
            artifact_path,
            data,
        )

    # -----------------------------------------
    # Finish
    # -----------------------------------------

    def finish(self) -> None:

        if self._finished:
            return

        # 먼저 background 작업 종료
        self._system_sampler.stop()

        self._restore_output()

        self._http_client.post_json(
            (
                f"/api/v1/sdk/runs/"
                f"{self._run_id}/complete"
            ),
            {},
        )

        self._finished = True

    def fail(
        self,
        error: str,
    ) -> None:

        if self._finished:
            return

        self._system_sampler.stop()

        self._restore_output()

        self._http_client.post_json(
            (
                f"/api/v1/sdk/runs/"
                f"{self._run_id}/fail"
            ),
            {
                "error": error
            },
        )

        self._finished = True

    # -----------------------------------------
    # stdout / stderr
    # -----------------------------------------

    def _install_output_capture(self) -> None:

        self._original_stdout = sys.stdout
        self._original_stderr = sys.stderr

        sys.stdout = LabFlowStream(
            original=self._original_stdout,
            stream_name="STDOUT",
            run_id=self._run_id,
            http_client=self._http_client,
            started_at=self._started_at,
        )

        sys.stderr = LabFlowStream(
            original=self._original_stderr,
            stream_name="STDERR",
            run_id=self._run_id,
            http_client=self._http_client,
            started_at=self._started_at,
        )

    def _restore_output(self) -> None:

        if self._original_stdout is not None:

            try:
                sys.stdout.flush()
            except Exception:
                pass

            sys.stdout = self._original_stdout

            self._original_stdout = None

        if self._original_stderr is not None:

            try:
                sys.stderr.flush()
            except Exception:
                pass

            sys.stderr = self._original_stderr

            self._original_stderr = None

    # -----------------------------------------
    # Validation
    # -----------------------------------------

    @staticmethod
    def _validate_metrics(
        *,
        epoch: int | None,
        step: int | None,
        metrics: dict[str, object],
    ) -> None:

        if epoch is not None:

            if not isinstance(epoch, int):
                raise TypeError(
                    "epoch must be an integer"
                )

            if epoch < 0:
                raise ValueError(
                    "epoch must not be negative"
                )

        if step is not None:

            if not isinstance(step, int):
                raise TypeError(
                    "step must be an integer"
                )

            if step < 0:
                raise ValueError(
                    "step must not be negative"
                )

        if not isinstance(metrics, dict):
            raise TypeError(
                "metrics must be a dictionary"
            )

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
                continue

            if isinstance(value, float):
                if not math.isfinite(value):
                    raise ValueError(
                        f"metric '{key}' "
                        f"must be finite"
                    )

    # -----------------------------------------
    # Utility
    # -----------------------------------------

    @staticmethod
    def _normalize_map(
        values: dict[str, object],
    ) -> dict[str, str]:

        normalized = {}

        for key, value in values.items():

            if not isinstance(key, str):
                raise TypeError(
                    "key must be a string"
                )

            normalized[key] = str(value)

        return normalized

    def _ensure_active(self) -> None:

        if self._finished:
            raise RuntimeError(
                "Run is already finished."
            )

    # -----------------------------------------
    # Context Manager
    # -----------------------------------------

    def __enter__(self):
        return self

    def __exit__(
        self,
        exc_type,
        exc_value,
        traceback,
    ):

        if exc_value is None:
            self.finish()
        else:
            self.fail(
                str(exc_value)
            )

        # 원래 예외는 다시 발생시킴
        return False