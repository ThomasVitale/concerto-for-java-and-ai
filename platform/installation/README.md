# Getting Started with the Kadras Engineering Platform

This guide describes how to install the Kadras Engineering Platform on a cloud Kubernetes cluster.

## Cloud Installation

### 0. Before you begin

To follow the guide, ensure you have the following tools installed in your local environment:

* Kubernetes [`kubectl`](https://kubectl.docs.kubernetes.io/installation/kubectl)
* Carvel [`kctrl`](https://carvel.dev/kapp-controller/docs/latest/install/#installing-kapp-controller-cli-kctrl)
* Carvel [`kapp`](https://carvel.dev/kapp/docs/v0.62.x/install).

### 1. Deploy Carvel kapp-controller

The platform relies on the Kubernetes-native package management capabilities offered by Carvel [kapp-controller](https://carvel.dev/kapp-controller). You can install it with Carvel [`kapp`](https://carvel.dev/kapp/docs/latest/install) (recommended choice) or `kubectl`.

```shell
kapp deploy -a kapp-controller -y \
  -f https://github.com/carvel-dev/kapp-controller/releases/latest/download/release.yml
```

### 2. Add the Kadras Package Repository

Add the Kadras repository to make the platform packages available to the cluster.

```shell
kctrl package repository add -r kadras-packages \
  --url ghcr.io/kadras-io/kadras-packages:0.21.0 \
  -n kadras-system --create-namespace
```

### 3. Create the Secrets for the Developer Portal

The platform includes a developer portal based on Backstage, which is integrated with GitHub out-of-the-box. To make the integration work, you need to generate a Personal Access Token (PAT) from your GitHub account. Furthermore, you need to create an OAuth App on GitHub and get a pair of client ID and client secret. Follow the [official documentation](https://backstage.io/docs/auth/github/provider#create-an-oauth-app-on-github) for creating the OAuth App.

```shell script
export GITHUB_TOKEN=<github-token>
export AUTH_GITHUB_CLIENT_ID=<github-app-client-id>
export AUTH_GITHUB_CLIENT_SECRET=<github-app-client-secret>
```

Then, store the credentials in a dedicated Secret on the cluster.

```shell script
kubectl create namespace backstage
kubectl create secret generic developer-portal-secrets \
  --from-literal=GITHUB_TOKEN="${GITHUB_TOKEN}" \
  --from-literal=AUTH_GITHUB_CLIENT_ID="${AUTH_GITHUB_CLIENT_ID}" \
  --from-literal=AUTH_GITHUB_CLIENT_SECRET="${AUTH_GITHUB_CLIENT_SECRET}" \
  --namespace=backstage
```

### 4. Configure the Platform

The installation of the Kadras Engineering Platform can be configured via YAML. A `values-cloud.yml` file is provided in the current folder with configuration to customize the cloud installation of the platform, based on the `run` installation profile. Make sure to update the domain names included in the YAML file with one of yours.

### 5. Install the Platform

Reference the `values-cloud.yml` file mentioned in the previous step and install the Kadras Engineering Platform.

```shell
kctrl package install -i engineering-platform \
  -p engineering-platform.packages.kadras.io \
  -v 0.19.0 \
  -n kadras-system \
  --values-file values-cloud.yml
```

### 5. Verify the Installation

Verify that all the platform components have been installed and properly reconciled.

```shell
kctrl package installed list -n kadras-system
```

A GitOps reconciliation strategy is used to install data services and applications. You can check the sync status as follows.

```shell
kubectl get kustomization gitops-configurer -n kadras-system
```

### 7. Configure OpenAI

If you want to run LLM-powered applications on the platform and integrate with OpenAI, you need to configure your own API Key.

```shell script
export OPENAI_API_KEY=<openai-api-key>
```

Then, store the API key in a dedicated Secret on the cluster.

```shell script
kubectl create secret generic openai-secret \
  --from-literal=api-key="${OPENAI_API_KEY}" \
  --namespace=kadras-system
```

### 6. Accessing Grafana

If you want to access Grafana, you can get the credentials from the dedicated Secret on the cluster.

```shell script
echo "Admin Username: $(kubectl get secret --namespace observability-stack loki-stack-grafana -o jsonpath="{.data.admin-user}" | base64 --decode)"
echo "Admin Password: $(kubectl get secret --namespace observability-stack loki-stack-grafana -o jsonpath="{.data.admin-password}" | base64 --decode)"
```

It will be available at [https://grafana.cloud.thomasvitale.dev](https://grafana.cloud.thomasvitale.dev).
