# Oracle Smart Backend — OKE Deployment Guide

This guide covers building the Spring Boot backend, pushing the image to Oracle Cloud Infrastructure Registry (OCIR), and deploying to Oracle Kubernetes Engine (OKE) in the `green` namespace.

## Project summary

| Item | Value |
|------|-------|
| Build tool | Maven |
| Java version | 17 |
| Spring Boot | 3.3.5 |
| Main class | `com.example.omi.OracleSmartApplication` |
| Server port | `8080` |
| API base path | `/api` (all REST controllers use `/api/...`) |
| Health check | `GET /api/health` (includes DB check) |
| JAR artifact | `target/omi-backend-0.0.1-SNAPSHOT.jar` |
| Database | Oracle Autonomous Database (wallet + TNS) |

### Required environment variables

| Variable | Description |
|----------|-------------|
| `ADB_TNS_ALIAS` | TNS alias from `tnsnames.ora` (e.g. `adb_high`) |
| `TNS_ADMIN` | Wallet directory inside the container (`/app/wallet`) |
| `ADB_APP_USER` | Database username |
| `ADB_APP_PASSWORD` | Database password |
| `FRONTEND_ORIGIN` | Allowed CORS origin(s), comma-separated if multiple |

The wallet is **not** baked into the Docker image. Mount it at runtime at `/app/wallet` (same path used by `docker-compose.yml` locally).

---

## 1. Build the JAR locally

From the `Oracle_backend` directory:

```bash
cd Oracle_backend
mvn clean package -DskipTests
```

Verify the JAR exists:

```bash
ls -la target/omi-backend-0.0.1-SNAPSHOT.jar
```

---

## 2. Build the Docker image

```bash
docker build -t oracle-smart-backend:local .
```

The Dockerfile uses a multi-stage build (Maven 17 → JRE 17), copies `omi-backend-0.0.1-SNAPSHOT.jar` as `app.jar`, and exposes port `8080`. It does **not** include wallets, `.env` files, or credentials.

---

## 3. Tag for OCIR (Querétaro region)

Your namespace is `axozuy0c3gbd`:

```bash
export OCIR_REGION=qro.ocir.io
export OCIR_NAMESPACE=axozuy0c3gbd
export IMAGE_NAME=oracle-smart-backend
export IMAGE_TAG=v1

docker tag oracle-smart-backend:local \
  ${OCIR_REGION}/${OCIR_NAMESPACE}/${IMAGE_NAME}:${IMAGE_TAG}
```

Full image reference used by Kubernetes:

```
qro.ocir.io/axozuy0c3gbd/oracle-smart-backend:v1
```

---

## 4. Log in and push to OCIR

Generate an auth token in OCI Console: **Profile → User Settings → Auth Tokens**.

```bash
docker login ${OCIR_REGION} \
  -u '<tenancy-namespace>/<oci-username>' \
  -p '<auth-token>'
```

Push:

```bash
docker push ${OCIR_REGION}/${OCIR_NAMESPACE}/${IMAGE_NAME}:${IMAGE_TAG}
```

Ensure the cluster has `imagePullSecrets: ocir-secret` in namespace `green` (created during frontend setup).

---

## 5. Create Kubernetes secrets

### 5.1 Database credentials

Never commit real passwords. Create the secret in the cluster:

```bash
kubectl create namespace green --dry-run=client -o yaml | kubectl apply -f -

kubectl create secret generic adb-credentials \
  --namespace=green \
  --from-literal=ADB_TNS_ALIAS='your_tns_alias' \
  --from-literal=ADB_APP_USER='your_db_user' \
  --from-literal=ADB_APP_PASSWORD='your_db_password' \
  --from-literal=FRONTEND_ORIGIN='https://your-ingress-host'
```

### 5.2 Oracle Autonomous Database wallet

1. Download the wallet zip from OCI Console (Autonomous Database → DB Connection → Download wallet).
2. Unzip locally into a folder (e.g. `config/wallet/`) containing `tnsnames.ora`, `sqlnet.ora`, `cwallet.sso`, etc.
3. **Do not** commit the zip or wallet files to git.

Create a Kubernetes secret from the wallet directory:

```bash
kubectl create secret generic adb-wallet \
  --namespace=green \
  --from-file=wallet/
```

The deployment mounts this secret at `/app/wallet` and sets `TNS_ADMIN=/app/wallet`, matching `application.yml`:

```yaml
url: jdbc:oracle:thin:@${ADB_TNS_ALIAS}?TNS_ADMIN=${TNS_ADMIN}
```

---

## 6. Apply Kubernetes manifests

From `Oracle_backend`:

```bash
kubectl apply -f k8s/backend/backend-deployment.yaml
kubectl apply -f k8s/backend/backend-service.yaml
kubectl apply -f k8s/ingress.yaml
```

### Ingress routing

| Path | Service | Port |
|------|---------|------|
| `/` | `frontend-green-service` | 80 |
| `/api` | `backend-service` | 8080 |

Paths are forwarded **without** stripping `/api`, which matches Spring controllers and the React frontend (`fetch('/api/...')`).

If an ingress named `oracle-smart-ingress` already exists in `green`, merge rules manually instead of creating a duplicate.

---

## 7. Validate deployment

```bash
kubectl get pods -n green -l app=oracle-smart-backend
kubectl logs -n green -l app=oracle-smart-backend --tail=100
kubectl port-forward -n green svc/backend-service 8080:8080
curl http://localhost:8080/api/health
```

Through ingress:

```bash
curl -s https://<ingress-host>/api/health
```

Expected health response:

```json
{"status":"ok","db":1}
```

---

## 8. Local development (Docker Compose)

Create these files locally (not committed to git):

```text
Oracle_backend/
├── config/
│   ├── .env
│   └── wallet/          ← unzip OCI wallet here
```

Example `config/.env`:

```env
ADB_TNS_ALIAS=adb_high
ADB_APP_USER=your_user
ADB_APP_PASSWORD=your_password
FRONTEND_ORIGIN=http://localhost:5173
```

Run:

```bash
docker compose up --build
```

---

## 9. Troubleshooting

| Symptom | Likely cause |
|---------|----------------|
| `ImagePullBackOff` | Wrong OCIR tag, missing `ocir-secret`, or registry login |
| Readiness probe failing | Wallet not mounted, wrong `ADB_TNS_ALIAS`, or DB credentials |
| `ORA-12154` / TNS errors | Wallet secret missing files or `TNS_ADMIN` not `/app/wallet` |
| CORS errors in browser | `FRONTEND_ORIGIN` does not match the frontend URL |
| 502 on `/api` | Backend pods not ready; check logs and DB connectivity |

---

## File layout

```
Oracle_backend/
├── Dockerfile
├── .dockerignore
├── DEPLOYMENT.md
├── docker-compose.yml
├── pom.xml
├── src/
└── k8s/
    ├── ingress.yaml
    └── backend/
        ├── backend-deployment.yaml
        └── backend-service.yaml
```
