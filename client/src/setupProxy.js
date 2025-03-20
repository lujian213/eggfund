const {createProxyMiddleware} = require("http-proxy-middleware");

//https://103.200.29.147:9011

module.exports = function (app) {
    app.use(
        "/investor",
        createProxyMiddleware({
            target: 'https://eggfund.website/',
            changeOrigin: true,
            secure: false,
        })
    );
    app.use(
        "/invest",
        createProxyMiddleware({
            target: 'https://eggfund.website/',
            changeOrigin: true,
            secure: false,
        })
    );
    app.use(
        "/fund",
        createProxyMiddleware({
            target: 'https://eggfund.website/',
            changeOrigin: true,
            secure: false,
        })
    );
    app.use(
        "/value",
        createProxyMiddleware({
            target: 'https://eggfund.website/',
            changeOrigin: true,
            secure: false,
        })
    );
    app.use(
        "/summary",
        createProxyMiddleware({
            target: 'https://eggfund.website/',
            changeOrigin: true,
            secure: false,
        })
    );
    app.use(
        "/values",
        createProxyMiddleware({
            target: 'https://eggfund.website/',
            changeOrigin: true,
            secure: false,
        })
    );
    app.use(
        "/invests",
        createProxyMiddleware({
            target: 'https://eggfund.website/',
            changeOrigin: true,
            secure: false,
        })
    );
    app.use(
        "/investors",
        createProxyMiddleware({
            target: 'https://eggfund.website/',
            changeOrigin: true,
            secure: false,
        })
    );
    app.use(
        "/funds",
        createProxyMiddleware({
            target: 'https://eggfund.website/',
            changeOrigin: true,
            secure: false,
        })
    );
    app.use(
        "/rtvalues",
        createProxyMiddleware({
            target: 'https://eggfund.website/',
            changeOrigin: true,
            secure: false,
        })
    );
    app.use(
        "/disableinvest",
        createProxyMiddleware({
            target: 'https://eggfund.website/',
            changeOrigin: true,
            secure: false,
        })
    );
    app.use(
        "/uploadinvests",
        createProxyMiddleware({
            target: 'https://eggfund.website/',
            changeOrigin: true,
            secure: false,
        })
    );
    app.use(
        "/exportinvests",
        createProxyMiddleware({
            target: 'https://eggfund.website/',
            changeOrigin: true,
            secure: false,
        })
    );
}