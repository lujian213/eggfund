const {createProxyMiddleware} = require("http-proxy-middleware");

module.exports = function (app) {
    app.use(
        "/investor",
        createProxyMiddleware({
            target: 'test',
            changeOrigin: true,
            secure: false,
        })
    );
    app.use(
        "/invest",
        createProxyMiddleware({
            target: 'test',
            changeOrigin: true,
            secure: false,
        })
    );
    app.use(
        "/fund",
        createProxyMiddleware({
            target: 'test',
            changeOrigin: true,
            secure: false,
        })
    );
    app.use(
        "/value",
        createProxyMiddleware({
            target: 'test',
            changeOrigin: true,
            secure: false,
        })
    );
    app.use(
        "/summary",
        createProxyMiddleware({
            target: 'test',
            changeOrigin: true,
            secure: false,
        })
    );
    app.use(
        "/values",
        createProxyMiddleware({
            target: 'test',
            changeOrigin: true,
            secure: false,
        })
    );
    app.use(
        "/invests",
        createProxyMiddleware({
            target: 'test',
            changeOrigin: true,
            secure: false,
        })
    );
    app.use(
        "/investors",
        createProxyMiddleware({
            target: 'test',
            changeOrigin: true,
            secure: false,
        })
    );
    app.use(
        "/funds",
        createProxyMiddleware({
            target: 'test',
            changeOrigin: true,
            secure: false,
        })
    );
    app.use(
        "/rtvalues",
        createProxyMiddleware({
            target: 'test',
            changeOrigin: true,
            secure: false,
        })
    );
    app.use(
        "/disableinvest",
        createProxyMiddleware({
            target: 'test',
            changeOrigin: true,
            secure: false,
        })
    );
    app.use(
        "/uploadinvests",
        createProxyMiddleware({
            target: 'test',
            changeOrigin: true,
            secure: false,
        })
    );
    app.use(
        "/exportinvests",
        createProxyMiddleware({
            target: 'test',
            changeOrigin: true,
            secure: false,
        })
    );
}