import time
import labflow


labflow.init(
    api_key="lfp_test_xxxxx",
    name="sdk-test-run",
    metadata={
        "dataset": "dummy",
        "description": "SDK integration test",
    },
)

labflow.log_params({
    "model": "DummyModel",
    "optimizer": "AdamW",
    "learning_rate": 0.001,
    "batch_size": 32,
})

for epoch in range(2):
    for step in range(3):

        loss = 1.0 / (step + epoch + 1)

        labflow.log_metrics(
            epoch=epoch,
            step=epoch * 3 + step,
            metrics={
                "loss": loss,
                "accuracy": 0.8 + step * 0.01,
                "phase": "train",
            },
        )

        print(
            f"epoch={epoch}, "
            f"step={step}, "
            f"loss={loss}"
        )

        time.sleep(1)

labflow.finish()