# Specification

```{toctree}
:maxdepth: 2

specification_rt1 specification_rt2
```

FreeRails is a railway strategy and management game. The player develops a railroad across
a changing landscape, connects places that need transport, and turns a small network into
a thriving business. The game aims for engaging strategic choices rather than a complete
or realistic simulation.

This document describes the intended player experience and game rules. It focuses on what
players can do and how the world responds, not on how the game is implemented.

## Design goals

- Make building and operating a railroad the heart of play: players choose
where to invest, which places to serve, and how to grow their network.
- Offer a rich set of connected systems. Geography shapes construction;
stations connect local supply and demand to the network; trains turn routes into services;
and company finances reward or constrain expansion.
- Give players room to pursue different strategies, from careful service
planning to rapid expansion and competition with other railroads.
- Keep the rules understandable and the world legible, while leaving
interesting trade-offs for players to discover.

## The world and the passage of time

The world is a map of square tiles representing a broad region. Terrain, settlements,
resources, and industries vary across maps and scenarios, giving each network different
opportunities and construction challenges. Maps may range from compact tutorial settings
to large regional landscapes.

Game time is abstracted so that a relatively short play session can represent the
development of a railroad over many years. The economy and the world change as time
passes, creating new opportunities and changing the value of existing services.

## Building a railroad

Players extend their network by acquiring rights of way and building track. Terrain
affects the expense and difficulty of construction. Track can be improved, removed, and
extended from the existing network; bridges and tunnels allow routes through otherwise
difficult terrain. Building and upgrading require investment, while track and trains carry
ongoing costs.

Track is shared infrastructure with limited capacity. Trains must contend for available
routes, but a train that has stopped should not permanently prevent other trains from
moving. Route planning therefore balances direct connections, construction cost, terrain,
and the traffic the network can carry.

## Stations, cargo, and train services

Stations gather the goods and passengers produced or needed in the surrounding area. Their
service area and local development determine the traffic available to a railroad. Stations
must be placed so their service areas do not overlap. Station improvements can unlock
train construction or improve operations, maintenance, storage, and the return on selected
cargo.

The world produces and consumes a variety of cargo, including passengers, mail, and
freight. Cargo types differ in their delivery needs: some benefit more from speed, while
others are suited to bulk transport. Cargo waits at stations for service and may lose
value or be wasted over time, so reliable routes and suitable station facilities matter.

Players build trains from engines and cargo-carrying wagons, then schedule services
between stations. A train loads and unloads where it stops, and its consist can be changed
at scheduled stations. Train performance depends on its engine, consist, cargo, and the
terrain along its route. Trains find routes automatically, with priority services
receiving precedence when routes are contested.

## Running a railroad

Delivering cargo earns revenue and connects the quality of a railroad's services to its
financial success. Players must balance expansion and improvements against construction,
operating, and maintenance costs. Changing economic conditions affect the cost of
investment and the terms of borrowing.

Companies can raise and repay bonds, issue and trade shares, and compete for control of
rival railroads. Gaining control can provide influence over a rival; complete acquisition
can combine its assets with the player's railroad. Financing and competition provide
alternatives to growth through construction alone, but they also create financial risk.

## A changing economy and landscape

Cities, industries, and resource-producing places respond over time to their economic
surroundings and to the traffic delivered by railroads. Settlements can grow or decline,
and the location and mix of economic activity can change. These changes affect where cargo
is available and where new connections may become worthwhile.

## Play against other railroads

Players may develop their railroad alone or compete with other human or
computer-controlled railroads. Every railroad should operate within the same game rules,
and competition should emerge from decisions about service, investment, and control of
companies rather than from separate rule sets.

## Visual and interaction style

The map is the main way players understand and shape the world. It should make terrain,
settlements, stations, track, and moving trains easy to distinguish, with enough regional
context to support planning and enough local detail to make construction and operations
clear.

The presentation should be readable and informative rather than ornamental. Important
changes in the railroad and its finances should be easy to recognize, and reports should
help players understand the state of their company and services without obscuring the
world.

Interaction should make planning feel direct. Players should be able to inspect the map,
choose an action, see clear feedback about its likely result, and confirm or cancel it
without losing their place. Invalid or unaffordable actions should be apparent before they
cause confusion. Mouse and keyboard input should both support common map and management
tasks, with consistent controls and locally available, localized help.
