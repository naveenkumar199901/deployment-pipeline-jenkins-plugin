package com.pearson.deployment
import org.yaml.snakeyaml.Yaml

class KubeResource {
  LinkedHashMap config
  private KubeWrapper kube
  LinkedHashMap attr
  String klass
  String namespace
  private def yaml

  KubeResource(String klass, String namespace, LinkedHashMap config) {
    this.config = config
    this.namespace = namespace
    this.klass = klass
    this.kube = new KubeWrapper(klass, namespace)
    this.yaml = new Yaml()
  }

  def get(def name) {
    try {
      def data = kube.get(name)
      def existingResource =  yaml.load(data)
      def existingConfig = specToConfig(existingResource)
      return new KubeResource(klass, namespace, existingConfig)
    } catch(all) {
      println all
      return null
    }
  }

  def createOrUpdate() {
    def existing = get(config.name)

    if (existing) {
      println " NO EXISTING RESOURCE"
      if (existing != this) {
        println "MUST UPDATE"
        update()
      }
    } else {
      create()
    }
  }

  def create() {
    println "Must create new ${namespace}/${klass} resource"
    def contents = yaml.dumpAsMap(configToSpec())

    def writer = new File(resourceFilename())
    writer.write contents
    kube.create(resourceFilename())
  }

  def update() {
    println "Must update new ${namespace}/${klass} resource"
    def contents = yaml.dumpAsMap(configToSpec())

    def writer = new File(resourceFilename())
    writer << contents
    kube.apply(resourceFilename())
  }

  private def resourceFilename() {
    "/tmp/${namespace}-${klass}-${config.name}.yaml"
  }
}
