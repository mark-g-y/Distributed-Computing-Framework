set "args=%*"
set "dir=%~dp0"
java -cp "%dir%/lib/*;%dir%/target/*" com.distributedcomputing.App %args%
