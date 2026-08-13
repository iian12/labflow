from labflow.client import LabFlowClient
from labflow.config import LabFlowConfig
from labflow.run import ActiveRun


_client: LabFlowClient | None = None
_active_run: ActiveRun | None = None


def init(
    *,
    api_key: str,
    name: str,
    metadata: dict[str, object] | None = None,
    base_url: str = "http://localhost:8080",
    capture_output: bool = True,
    system_metric_interval_seconds: float = 1.0,
) -> ActiveRun:

    global _client
    global _active_run

    if _active_run is not None:
        raise RuntimeError(
            "A LabFlow run is already active."
        )

    config = LabFlowConfig(
        api_key=api_key,
        base_url=base_url,
        capture_output=capture_output,
        system_metric_interval_seconds=(
            system_metric_interval_seconds
        ),
    )

    _client = LabFlowClient(
        config
    )

    _active_run = _client.start_run(
        name=name,
        metadata=metadata,
    )

    return _active_run


def log_params(
    params: dict[str, object],
) -> None:

    run = _require_active_run()

    run.log_params(params)


def log_metrics(
    *,
    metrics: dict[str, object] | None = None,
    epoch: int | None = None,
    step: int | None = None,
) -> None:

    run = _require_active_run()

    run.log_metrics(
        metrics=metrics,
        epoch=epoch,
        step=step,
    )


def log_artifact(
    *,
    path: str,
    artifact_type: str,
    name: str | None = None,
    epoch: int | None = None,
    step: int | None = None,
    metadata: dict[str, object] | None = None,
) -> None:

    run = _require_active_run()

    run.log_artifact(
        path=path,
        artifact_type=artifact_type,
        name=name,
        epoch=epoch,
        step=step,
        metadata=metadata,
    )


def finish() -> None:

    global _client
    global _active_run

    if _active_run is None:
        return

    try:
        _active_run.finish()

    finally:
        if _client is not None:
            _client.close()

        _active_run = None
        _client = None


def _require_active_run() -> ActiveRun:

    if _active_run is None:
        raise RuntimeError(
            "LabFlow is not initialized. "
            "Call labflow.init() first."
        )

    return _active_run