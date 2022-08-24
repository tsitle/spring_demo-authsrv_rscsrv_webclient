db.createUser(
{
    user: "rscsrv",
    pwd: "abcd",
    roles: [{role: "readWrite", db: "oauth_rsc_server_demo"}]
});
