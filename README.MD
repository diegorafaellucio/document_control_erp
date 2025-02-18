# Projeto Getdoc 5 api 

Para rodar esse projeto é necessário:
 - maven 3.5 ou superior 
 - plugin do lombok 



* Atenção criar o arquivo settings.xml (C:\Users\user\\.m2\settings.xml) do maven para baixar as dependências no Nexus.

        <?xml version="1.0" encoding="UTF-8"?>
        <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
                  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
          
          <pluginGroups>
          </pluginGroups>
        
          <proxies>
          </proxies>
        
          <servers>
            <server>
              <id>nexus_getdoc</id>
              <username>admin</username>
              <password>7vRP5NKfbnk9rFpc</password>
            </server>
          </servers>
        
          <mirrors>
          </mirrors>
        
          <profiles>
          </profiles>
          
        </settings>
    
* Adicionar os seguintes parametros no tomcat 9 para executar o projeto

        -Xmx1g
        -Dgetdoc.mode=dev
        -Dgetdoc.server=local
        -Dgetdoc.logarAcessos=true
        -Dgetdoc.emailException=true
        -Dgetdoc.executarOcr=true

* Os logs por padrão vão para a pasta `/opt/jboss/wildfly/standalone/log`. Para alterar, é só adicionar a variável `-DlogPath=<caminho>`.

* O wildfly vai capturar todos os logs do sistema e enviar para o "server.log". Para evitar isso, deve ser feito as  seguintes alterações no standalone.xml:

    - adicionar parâmetro `profile -> subsystem xmlns="urn:jboss:domain:logging:5.0" -> <add-logging-api-dependencies value="false"/>`
    - adicionar logger `profile -> subsystem xmlns="urn:jboss:domain:logging:5.0" -> <logger category="stdout" use-parent-handlers="false">
                                                                                    <level name="OFF"/>
                                                                                </logger>`
* URL Swagger

        - http://localhost:8082/getdoc/rest/swagger-ui.html
        - http://localhost:8080/getdoc_aluno/rest/v2/api-docs