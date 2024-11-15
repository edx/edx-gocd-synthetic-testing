# Generating the jar file

version 1.0.1

1. Install maven (I've used version 3.9.3)
2. At the repo's root folder, run "mvn clean package"
3. Find the synthetic-testing-0.1.0.jar file in the /target folder under the repo root folder

# Configuring the plugin
The plugin requires and expects no configuration data.
All required configuration data is hard-wired into the plugin for now

# Environment variables
The plugin does require that the GoCD process that spawns it have a couple of environment
variables set up, to allow it to access the Datadog API. These values need to be obtained from keys in
https://app.datadoghq.com/organization-settings/api-keys and 
https://app.datadoghq.com/organization-settings/application-keys. I've used the keys named 
Synthetics-private-location-edx-tools and gocd_experiments, respectively.

export DATADOG_API_KEY="Value goes here"
export DATADOG_APP_KEY="Value goes here"

# Requested SRE action
Please register the plugin with GoCD
