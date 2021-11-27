import requests
import os.path
import plantuml

# PLANTUML_RELEASE_URL = 'https://github.com/plantuml/plantuml/releases/download/v1.2021.14/plantuml-1.2021.14.jar'
# PLANTUML_JAR = 'plantuml.jar'
# if os.path.isfile(PLANTUML_JAR) is False:
#     r = requests.get(PLANTUML_RELEASE_URL, allow_redirects=True)
#     open(PLANTUML_JAR, 'wb').write(r.content)

# stream = os.popen('/usr/libexec/java_home -v11 plantuml.jar "./**.puml"')
# output = stream.read()

puml = plantuml.PlantUML(url='http://www.plantuml.com/plantuml/img/')


for file in os.listdir("./"):
    if file.endswith(".puml"):
        input_file = file;
        output_file = "../images/" + file.replace(".puml", ".png")
        puml.processes_file(input_file, output_file)
        print("Diagram", output_file, "is generated")