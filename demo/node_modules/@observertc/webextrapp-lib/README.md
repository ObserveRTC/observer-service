# Wobserver WebRTC Extractor
Extractor Development Toolkits for WebRTC Samples


# Build library locally

- ### Install dependencies
  - Make sure we have type script installed in the system
    - npm install -g typescript`
  - Install package dependencies 
    - `npm ci`
  - Build the library 
    - `npm run build-library`

  Once build is complete it will create `webextrapp-lib.js` library in the `dist` folder. 

- ### Publish the package

  - We are using github package manager to publish the library
  - Goto the `package.json` and update the version
  - Login to GitHub package registry using your credentials
    - ​    `npm login --registry=https://npm.pkg.github.com`
  - Publish the build `webextrapp-lib.js` package
    - `npm publish`



## Use the library in a project

- ### Install package

  - Add the package in your package.json
    - `"@observertc/webextrapp-lib": "0.0.3"`
  - Create `.npmrc` in the project folder and add our registry
    - `@observertc:registry=https://npm.pkg.github.com/`
  - Install the package
    - `npm install`

- ### How to use the library
 
