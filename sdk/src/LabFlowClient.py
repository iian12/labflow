
from config import LabFlowConfig
from service import MetricService
from run import ActiveRun
from collector import SystemMetricCollector
from src.http_client import HttpClient


class LabFlowClient:

    def __init__(self, config: LabFlowConfig):
        self._http = HttpClient(config)

        self._metric_service = MetricService(self._http)
        self._system_collector = SystemMetricCollector()

    def start_run(self, name: str) -> ActiveRun:
        response = self._http.post(
            "/api/v1/sdk/runs",
            {
                "name": "name",
            },
        )

        run_id = int(response["runId"])

        return ActiveRun(
            run_id=run_id,
            metric_service=self._metric_service,
            system_collector=self._system_collector,
        )

