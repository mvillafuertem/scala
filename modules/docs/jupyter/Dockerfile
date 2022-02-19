# We'll start from a minimal Jupyter image
# Also see https://jupyter-docker-stacks.readthedocs.io/en/latest/using/selecting.html
FROM jupyter/base-notebook

# Install Scala and Almond
# Based on Dockerfiles from https://github.com/almond-sh/almond
# and https://github.com/almond-sh/docker-images/tree/coursier
USER root

RUN apt-get -y update && \
    apt-get install --no-install-recommends -y \
      curl \
      openjdk-8-jre-headless \
      ca-certificates-java && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

RUN curl -Lo /usr/local/bin/coursier https://github.com/coursier/coursier/releases/download/v2.0.0-RC3-2/coursier && \
    chmod +x /usr/local/bin/coursier

USER $NB_UID
ENV SCALA_VERSION 2.13.4
ENV ALMOND_VERSION 0.11.2

# Install Almond kernel
RUN coursier bootstrap \
      -r jitpack \
      -i user -I user:sh.almond:scala-kernel-api_$SCALA_VERSION:$ALMOND_VERSION \
      sh.almond:scala-kernel_$SCALA_VERSION:$ALMOND_VERSION \
      --default=true --sources \
      -o almond \
      -M almond.ScalaKernel

RUN coursier bootstrap \
  --java-opt -XX:+UseG1GC \
  --java-opt -XX:+UseStringDeduplication  \
  --java-opt -Xss6m \
  --java-opt -Xms150m \
  org.scalameta:metals_2.12:0.11.1 -o metals -f

RUN ./almond --install --log info --metabrowse --id scala$SCALA_VERSION --display-name "Scala $SCALA_VERSION"

RUN echo $' \n\
c.LanguageServerManager.language_servers = {\n\
    "scala": {\n\
        # if installed as a binary\n\
        "argv": ["/home/jovyan/metals"],\n\
        "languages": ["scala"],\n\
        "version": 2\n\
    }\n\
}' >> /etc/jupyter/jupyter_server_config.py

RUN rm -f almond && rm -fr .cache