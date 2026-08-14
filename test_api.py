import urllib.request
import json

data = json.dumps({"email": "siddarthpatil17235@gmail.com", "password": "password"}).encode("utf-8")
req = urllib.request.Request("http://localhost:8080/api/users/login", data=data, headers={"Content-Type": "application/json"})
try:
    with urllib.request.urlopen(req) as response:
        res = json.loads(response.read().decode())
        token = res.get("data", {}).get("token")
        
        req2 = urllib.request.Request("http://localhost:8080/api/users/list", headers={"Authorization": "Bearer " + token})
        with urllib.request.urlopen(req2) as r2:
            print(r2.read().decode())
except urllib.error.HTTPError as e:
    print(e.code, e.read().decode())
