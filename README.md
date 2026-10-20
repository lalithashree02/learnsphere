# learnsphere

## Provisioning the first admin account

The application can create an initial admin account when both `ADMIN_BOOTSTRAP_EMAIL` and `ADMIN_BOOTSTRAP_PASSWORD` are set in the deployment environment. The password must be at least 12 characters; it is stored using BCrypt. On startup, the app creates the account only if that email is not already present and never changes an existing account's password.

Set both variables in the hosting provider and redeploy. After the admin account has been created, remove the bootstrap password variable from the deployment settings. Do not commit admin credentials or insert plaintext passwords into the database.