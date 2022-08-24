db.createUser(
{
    user: "authsrv",
    pwd: "abcd",
    roles: [{role: "readWrite", db: "oauth_auth_server_demo"}]
});
