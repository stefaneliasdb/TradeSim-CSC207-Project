# TradeSim
## Team Information
### Team Members
- Stefan Elias Davidsson Berman
- Harris Tang
- Nuerlan Muheyati
- Gil Shamir
---
## Project Description
### Domain
TradeSim is a Java-based paper trading and portfolio analytics application developed using Clean Architecture and SOLID design principles. The platform aims to integrate real-time market data, simulated stock trading, portfolio management, and financial analytics, providing users with an interactive environment to explore financial markets and evaluate investment strategies.
---
## User Stories
### As a trader, I want to be able to view stocks and analytics live.
### As a trader, I want to be able to view the history of my networth on the trading app.
### As a trader, I want to be able to buy and sell stocks.
### As a trader, I want to view the amount and performance of each stock I own.
---
## Use Cases
### View live stock analytics.
### Display history of user's networth.
### Place a buy or sell order.
### View owned stocks and performance.
---
## Minimal Viable Product (MVP)
| Team Member | MVP Use Case |
|-------------|--------------|
| Stefan | View live stock analytics |
| Harris | Display history of user's networth|
| Nuerlan | Place a buy or sell order |
| Gil | View owned stocks and performance |
---
## Entities
### Stock
### Portfolio
### Order
Represents a completed buy or sell transaction. Stores the order ID,
user ID, stock symbol, order type, quantity, execution price, and
execution time. Validates its inputs and calculates the total trade value.
### User
Represents a registered user. Stores userId, password, username, and amount of cash the user has. 
---
## API Usage
**API Name: Tickerbot**
**Documentation: [https://tickerbot.io/]**
**How it is used in the program: The API retrieves real-time stock market data, such as share prices, which the TradeSim program uses to display up-to-date financial information to users.**
---
## Design Diagrams
### UML Class Diagram
### Sequence Diagrams
---
## How to Run
[Instructions for running the program.]
---
## Notes
[Any additional project information]
