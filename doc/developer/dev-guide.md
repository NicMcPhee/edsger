# Hitchhiker's Guide to Edsger Development 

Edsger is a front-end only application fully written in ClojureScript. It is currently hosted in GitHub pages. The following are the structure of the project:

### Build & Testing
* `leiningen`: build & dependency management system.
* `doo, karma`: test runner.
* `clojure.test`: testing framework.
* `Travis CI`: CI & build system. When the unit tests passed in the `master` branch, `Travis CI` automatically pushes the changes to the branch for deployment.
* `Figwheel`: used for live code reloading.

### Current Project Set-up
* ClojureScript is the main development language. We also use [`Google Closure`](https://github.com/google/closure-library) for dom manipulation.
* `edsger.core` is the controller containing all input handlers.
  The `validate-handler` function is the only place that the frontend calls out to code in our other namespaces.
* `edsger.parsing` is a namespace for parsing user input strings into ClojureScript data structures using the [`instaparse`](https://github.com/Engelberg/instaparse) library.
  The function `check-match-recursive` is currently the only one intended for use outside the namespace.
* `edsger.unification` is a namespace containing validation functions using [`core.logic`](https://github.com/clojure/core.logic) library.
   The functions `parse` and `rulify` are the only ones intended for use outside the namespace.

### Known Issues & TODOs
* #39 Currently, the cursor shows unexpected behavior. This is because we manually reset the cursor location after we replace symbols in the input box.
* #36 An easy way to type rules is desirable. (e.g. rule dropdown)
* #32, #33 More helpful error messages are needed when parsing or evaluation fails.

### Adding tests

If you introduce a new namespace under `src/` you should also introduce a new
test namespace under `test/`. Be sure to import the new test namespace in
`edsger.test-runner` so that your new tests are actually run.

### File layout

- `dev` - something to do with figwheel
- `doc` - various documentation related to the project
- `resources/public` - this folder stores static resources to be deployed with
  the project.
- `resources/public/js/compiled` - this folder contains the results of compiling
  the ClojureScript to JS and is thus in `.gitignore`
- `src/edsger` - ClojureScript source code
- `test/edsger` - ClojureScript tests

### Deploying the project

To deploy the project on a real server instead of just running Figwheel on your
local box, take the following steps.

- Cleanup non-minimized JS files: `rm -r resources/public/js/compiled`
- Build minimized JS files: `lein cljsbuild once min`
- Copy the contents of `resources/public` to the root of your HTTP server

These are the steps we use to deploy to TravisCI as configured by the
`before_deploy` and `deploy` entries in `.travis.yml`.
