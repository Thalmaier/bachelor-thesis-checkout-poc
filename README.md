# Proof-of-Concept of a Checkout-Solution

This project was created as a result of a bachelor thesis concerning the different design possibilities
for aggregates within the domain of an online shop checkout application. The software uses a domain-driven approach and
utilises a hexagonal architecture.

## Thesis Abstract



## Short Variant-Summaries

This proof-of-concept was implemented three times with three different aggregate designs to analyse the
upsides and drawbacks of each design.

### Variant A

![Variant A Overview](https://i.imgur.com/3HFGtOn.png)

Variant A features one big aggregate. This leads to a comparatively easy implementation and high performance due to
less database queries. However, simultaneous api calls on the same resources are not possible. 

### Variant D

![Variant D Overview](https://i.imgur.com/ncvIesm.png)

In this variant the basket was split into four aggregates. This enables simultaneous api calls on the same resources,
however the source code gets more complex and performance is generally lower than variant A. Further, variant D was
implemented two times. With 'Variant D Flags' the software tries to minimize the necessity of a recalculation by only
recalculating if changes were made to the basket that influences the combined price. In order to achieve this, the
aggregates need to have a flag indicating if it was changed or not. This results in more database access calls since
the flag needs to be reset for every aggregate if a calculation is done. 'Variant D Calculation' on the other hand
calculates the basket every time the client accesses it. More processing power is needed, but the amount of calls to the
database are kept at a minimum.

## Configuration

The software configuration is done via an application-config.yaml file located in the 'src/main/resources' directory.

## API Description

The API is documented with the openapi standard v3. The files can be found in the 'additional-resources' folder or
online on swaggerhub:  
- [Online API Documentation for Variant A](https://app.swaggerhub.com/apis/xThale/checkout-poc-variant-a)  
- [Online API Documentation for Variant D](https://app.swaggerhub.com/apis/xThale/checkout-poc-variant-a)