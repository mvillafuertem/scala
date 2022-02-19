# Scala playground

Learning and playing around with Scala through [Jupyter](https://jupyter.org/)
with the [Almond Scala kernel](https://almond.sh/).

## Set up

We'll use docker to easily create an environment
with all dependencies and tools.
Start with building a minimal Jupyter/Almond docker image:

    docker build -t jupyter-almond .

## Launch

Run the docker image to serve the notebooks, for example:

    docker run --rm -it -p 8888:8888 -v ../notebooks:/home/jovyan/work jupyter-almond

Of course it is possible to finetune some Jupyter aspects.
For example, change the port number (when it is already in use)
and predefine the Jupyter security token:

    docker run --rm -it -p 9999:8888 -v $PWD/notebooks:/home/jovyan/work -e JUPYTER_TOKEN="s" jupyter-almond

Then go to http://localhost:9999/tree/work?token=s to access the notebooks.

