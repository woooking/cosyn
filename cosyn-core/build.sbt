name := "cosyn-core"

libraryDependencies += ("edu.stanford.nlp" % "stanford-corenlp" % "3.9.2").
    exclude("com.sun.xml.bind", "jaxb-impl")

libraryDependencies += "com.softwaremill.macwire" %% "macros" % "2.3.2" % "provided"
