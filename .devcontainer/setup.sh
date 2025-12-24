#!/bin/bash
# Adyen Kotlin Spring Online Payments devcontainer setup script

set -eo pipefail

echo "Set up Adyen Kotlin Spring Online Payments development environment"

# Check for required environment variables
missing_vars=()

[ -z "$ADYEN_API_KEY" ] && missing_vars+=("ADYEN_API_KEY")
[ -z "$ADYEN_CLIENT_KEY" ] && missing_vars+=("ADYEN_CLIENT_KEY")
[ -z "$ADYEN_MERCHANT_ACCOUNT" ] && missing_vars+=("ADYEN_MERCHANT_ACCOUNT")

echo ""
echo "This application uses Spring Boot's environment variable support."
echo "Set these environment variables before running the application:"
echo ""
echo "For GitHub Codespaces:"
echo "   1. Go to https://github.com/codespaces/secrets"
echo "   2. Add the following secrets:"
echo "      - ADYEN_API_KEY          (required) (https://docs.adyen.com/user-management/how-to-get-the-api-key)"
echo "      - ADYEN_CLIENT_KEY       (required) (https://docs.adyen.com/user-management/client-side-authentication)"
echo "      - ADYEN_MERCHANT_ACCOUNT (required) (https://docs.adyen.com/account/account-structure)"
echo "      - ADYEN_HMAC_KEY         (optional, recommended) (https://docs.adyen.com/development-resources/webhooks/verify-hmac-signatures)"
echo ""
echo "You can alternatively set the environment variables in the terminal:"
echo "   export ADYEN_API_KEY=yourAdyenApiKey"
echo "   export ADYEN_CLIENT_KEY=yourAdyenClientKey"
echo "   export ADYEN_MERCHANT_ACCOUNT=yourAdyenMerchantAccount"
echo "   export ADYEN_HMAC_KEY=yourHmacKey"
echo ""
echo "Remember to include http://*.github.dev/* and http://localhost:8080 in the Allowed Origins for your Client Key."
echo ""
echo "To start the application:"
echo "   ./gradlew bootRun"
echo ""
echo "Visit http://localhost:8080 to see the application running."

# Show missing variables warning at the very bottom (after any stacktraces)
if [ ${#missing_vars[@]} -gt 0 ]; then
    echo ""
    echo "⚠️  Missing required environment variables:"
    printf '   - %s\n' "${missing_vars[@]}"
else
    echo ""
    echo "✅ All required environment variables are set"
fi