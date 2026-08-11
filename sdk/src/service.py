from http_client import HttpClient
from event import MetricEvent


class MetricService:

    def __init__(self, http_client: HttpClient):
        self.http_client = http_client

    def send(self, run_id: int, event: MetricEvent) -> None:
        self.http_client.post(
            f"/api/v1/sdk/runs/{run_id}/metrics",
            event.to_payload(),
        )