# SpotifyApplication

This is Spotify Application that is built by Java Spring Boot.

PostgreSQL is used for database. 


Continious Integration and Continious Deployment is implemented in .gitlab-ci.yml file.


In this App, USERS can subscribe to 4 different Subscriptions. Each Subscription has different duration and monthly payment. 


Song and Category features are implemented in this project.

Each Song can have 0 or many Categories. Respectively, each Category may have 0 or many Songs. 


Categories may have Parent Categories -> Turkish Pop may be SubCategory of Pop Category;


English Rock may be a SubCategory of Rock.


Users can Like or Dislike Songs or Categories.


Users can listen (get) Songs and Categories. 


Every Month Invoices are created for active Subscription Records.


Caching is implemented to Reduce Number of Requests to DataBase.


And Many More Features...


Docker Compose is used in this project. Project is Deployed to EC2 Instance.
