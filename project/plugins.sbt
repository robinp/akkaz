resolvers += Resolver.url("scalasbt releases", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)

// idea
// no published version found for scala 2.10
//resolvers += "sbt-idea-repo" at "http://mpeltonen.github.com/maven/"

//addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.0.0")

// site

resolvers += "sonatype-releases" at "https://oss.sonatype.org/service/local/repositories/releases/content/"

addSbtPlugin("com.jsuereth" % "sbt-site-plugin" % "0.5.0")

// ghpages

resolvers += "jgit-repo" at "http://download.eclipse.org/jgit/maven"

addSbtPlugin("com.jsuereth" % "sbt-ghpages-plugin" % "0.4.0")