# [Knockoff - Markdown in Scala](http://tristanhunt.com/projects/knockoff) #

This is a fork of knockoff with some markdown extensions.

1. Wiki links like `[[this]]`
2. Template invocations like `{{this}}`
3. Ability to use markdown inside html tags, but...
4. Sanitizing inline html to *very* restrictive subset of it.

    import com.tristanhunt.knockoff.DefaultDiscounter._

    toXHTML(knockoff("""# My Markdown Content """))

You can use the blocks returned from the `knockoff` method to do useful things, like fetch the header:

    val blocks = knockoff("""# My markdown""")
    blocks.find( _.isInstanceOf[Header] ).map( toText ).getOrElse( "No header" )

## Using the latest version

The short story, in an sbt project/Build.scala file:

      lazy val root = Project("root", file(".")) dependsOn(knockoffProject)
      lazy val knockoffProject = RootProject(uri(
          "git://github.com/tristanjuricek/knockoff.git"))

The longer version can be read on this [nice dev daily overview](http://www.devdaily.com/scala/using-github-projects-scala-library-dependencies-sbt-sbteclipse).

I do publish versions to the sonatype repository.

## More information

See the [home page](http://tristanjuricek.com/knockoff) for more information: [http://tristanjuricek.com/knockoff]().

License is BSD. Patches are welcome, if the patch is clean, I'll probably accept it.
