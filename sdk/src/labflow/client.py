from labflow.api.http_client import HttpClient
from labflow.config import LabFlowConfig
from labflow.run import ActiveRun
from labflow.system.collector import (
    SystemMetricCollector,
)


class LabFlowClient:

    def __init__(
        self,
        config: LabFlowConfig,
    ):
        self._config = config
        self._http_client = HttpClient(config)

        self._system_collector = (
            SystemMetricCollector()
        )

    def start_run(
        self,
        *,
        name: str,
        metadata: dict[str, object] | None = None,
    ) -> ActiveRun:

        normalized_metadata = {
            key: str(value)
            for key, value
            in (metadata or {}).items()
        }

        response = self._http_client.post_json(
            "/api/v1/sdk/runs",
            {
                "name": name,
                "metadata": normalized_metadata,
            },
        )

        run_id = int(
            response["runId"]
        )

        return ActiveRun(
            run_id=run_id,
            http_client=self._http_client,
            system_collector=(
                self._system_collector
            ),
            system_metric_interval_seconds=(
                self._config
                .system_metric_interval_seconds
            ),
            capture_output=(
                self._config.capture_output
            ),
        )

    def close(self) -> None:
        self._http_client.close()