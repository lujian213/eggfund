const {createProxyMiddleware} = require("http-proxy-middleware");

//https://103.119.16.229:9011

//https://eggfund.website

module.exports = function (app) {
    app.use(
        "/login",
        createProxyMiddleware({
            target: 'https://103.119.16.229/',
            changeOrigin: true,
            secure: false,
        })
    );
    app.use(
        "/loginUser",
        createProxyMiddleware({
            target: 'https://103.119.16.229/',
            changeOrigin: true,
            secure: false,
        })
    );
    app.use(
        "/logout",
        createProxyMiddleware({
            target: 'https://103.119.16.229/',
            changeOrigin: true,
            secure: false,
        })
    );
    app.use(
        "/investor",
        createProxyMiddleware({
            target: 'https://103.119.16.229/',
            changeOrigin: true,
            secure: false,
        })
    );
    app.use(
        "/invest",
        createProxyMiddleware({
            target: 'https://103.119.16.229/',
            changeOrigin: true,
            secure: false,
        })
    );
    app.use(
        "/fund",
        createProxyMiddleware({
            target: 'https://103.119.16.229/',
            changeOrigin: true,
            secure: false,
        })
    );
    app.use(
        "/value",
        createProxyMiddleware({
            target: 'https://103.119.16.229/',
            changeOrigin: true,
            secure: false,
        })
    );
    app.use(
        "/summary",
        createProxyMiddleware({
            target: 'https://103.119.16.229/',
            changeOrigin: true,
            secure: false,
        })
    );
    app.use(
        "/values",
        createProxyMiddleware({
            target: 'https://103.119.16.229/',
            changeOrigin: true,
            secure: false,
        })
    );
    app.use(
        "/invests",
        createProxyMiddleware({
            target: 'https://103.119.16.229/',
            changeOrigin: true,
            secure: false,
        })
    );
    app.use(
        "/investors",
        createProxyMiddleware({
            target: 'https://103.119.16.229/',
            changeOrigin: true,
            secure: false,
        })
    );
    app.use(
        "/funds",
        createProxyMiddleware({
            target: 'https://103.119.16.229/',
            changeOrigin: true,
            secure: false,
        })
    );
    app.use(
        "/rtvalues",
        createProxyMiddleware({
            target: 'https://103.119.16.229/',
            changeOrigin: true,
            secure: false,
        })
    );
    app.use(
        "/disableinvest",
        createProxyMiddleware({
            target: 'https://103.119.16.229/',
            changeOrigin: true,
            secure: false,
        })
    );
    app.use(
        "/uploadinvests",
        createProxyMiddleware({
            target: 'https://103.119.16.229/',
            changeOrigin: true,
            secure: false,
        })
    );
    app.use(
        "/exportinvests",
        createProxyMiddleware({
            target: 'https://103.119.16.229/',
            changeOrigin: true,
            secure: false,
        })
    );
}