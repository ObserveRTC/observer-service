blabla it os only for those who develop and contribute...

THe generation of jooq is controlled manually. 
You need to set the `generateSchemaSourceOnCompilation = false` 
at build.grradle, and compile the project again (with running 
MySQL in the background with thee provided username and password 
configured in the build.gradle too).
And sometimes that does not work either! At this time I can suggest you 
to change the database the generation should apply to, 
and then change it back, so then it will work.