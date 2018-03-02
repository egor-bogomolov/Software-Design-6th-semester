First I went to popular and active [Awesome Kotlin](https://github.com/KotlinBy/awesome-kotlin)
GitHub repository, where information one can find information about decent 
libraries for Kotlin and searched for libraries for CLI argument parsing.

I got three results and compared them.
As a result I got the following table ("best" in each column would be check-marked):

library                       | popularity (stars + forks) | commits | last commit date) | license     
------------------------------|----------------------------|---------|-------------------|-------------
[leprosus/kotlin-cli](https://github.com/leprosus/kotlin-cli)           |   29+10                    |   34    | ✓ Feb 4 2018      | ✓ MIT       
[jimschubert/kopper](https://github.com/jimschubert/kopper)            |   37+2                     |   24    |   Dec 31 2016     | ✓ MIT       
[xenomachina/kotlin-argparser](https://github.com/xenomachina/kotlin-argparser)  | ✓ 213+18                   | ✓ 211   |   Jan 19 2018     | ✓ LGPL-2.1  

From there the choice was quite obvious, but I decided to look at usage examples for each library (table order preserved):
- option name to option are mapped using Map, standard features (defaults, descriptions...), limited casting possibilities (cannot read option and get its value as arbitrary class instance) 
- mapping using delegate properties, standard features, limited casting
- mapping using delegates, standard features, ability to map parsed argument

The last one seemed to be the most convenient one, which just concluded previous decision.

In other words, I decided to use xenomachina/kotlin-argparser.