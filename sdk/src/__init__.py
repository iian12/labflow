from LabFlowClient import LabFlowClient
from config import LabFlowConfig
from run import ActiveRun


_client: LabFlowClient | None = None
_active_run: ActiveRun | None = None

def init(*, api_key: str, run_name: str, base_url: str = "http://localhost:8080",) -> None:

    global _client, _active_run

    config = LabFlowConfig(
        api_key=api_key,
        base_url=base_url,
    )

    _client = LabFlowClient(config)

    _active_run = _client.start_run(name=run_name)

def log_metrics(
        *,
        epoch: int,
        step: int,
        metrics: dict[str, str]
) -> None:
    if _active_run is None:
        raise RuntimeError(
            "LabFlow is not initialized. "
            "Call labflow.init() first."
        )

    _active_run.log_metrics(
        epoch=epoch,
        step=step,
        metrics=metrics
    )



