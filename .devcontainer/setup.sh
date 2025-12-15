#!/bin/bash
# Adyen Kotlin Spring Online Payments devcontainer setup script

set -eo pipefail

echo "Set up Adyen Kotlin Spring Online Payments development environment"

echo "Before running the server, create a .env file in the project directory:"
echo "   cp .env.example .env"
echo "Then edit .env and fill in your actual values:"
echo "   - ADYEN_API_KEY          (required) (https://docs.adyen.com/user-management/how-to-get-the-api-key)"
echo "   - ADYEN_CLIENT_KEY       (required) (https://docs.adyen.com/user-management/client-side-authentication)"
echo "   - ADYEN_MERCHANT_ACCOUNT (required) (https://docs.adyen.com/account/account-structure)"
echo "   - ADYEN_HMAC_KEY         (optional, recommended) (https://docs.adyen.com/development-resources/webhooks/verify-hmac-signatures)"
echo "Remember to include http://*.github.dev/* and http://localhost:8080 in the Allowed Origins for your Client Key."
echo "To start the application:"
echo "   ./gradlew bootRun"
echo "Visit http://localhost:8080 to see the application running."