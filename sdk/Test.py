import requests

BASE_URL = "http://127.0.0.1:8080"
API_KEY = "kldfjkldajfkldjakldfjkldajfklajfl"

response = requests.post(
    f"{BASE_URL}/api/v1/sdk/test",
    headers={
        "Authorization": f"Bearer {API_KEY}",
        "Content-Type": "application/json",
    },
    json={
        "message": "Hello from Labflow SDK",
    },
    timeout=10,
)

response.raise_for_status()

print(response.status_code)
print(response.json())