if (process.env.NODE_ENV === "production") {
    const opt = require("./terraform-cdktf-opt.js");
    opt.main();
    module.exports = opt;
} else {
    var exports = window;
    exports.require = require("./terraform-cdktf-fastopt-entrypoint.js").require;
    window.global = window;

    const fastOpt = require("./terraform-cdktf-fastopt.js");
    fastOpt.main()
    module.exports = fastOpt;

    if (module.hot) {
        module.hot.accept();
    }
}
