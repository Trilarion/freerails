# Sid Meier's Railroad Tycoon (1990)

This specification describes the player-facing mechanics of the original *Railroad
Tycoon*. It focuses on the choices the player makes, the information the game presents,
and how the simulated railroad responds. Visual details are included where they help the
player read the map or operate the company; decorative presentation is not a goal here.

The primary evidence is the original manual and its companion technical supplement and
player-aid cards. The manual says it applies across computer systems (Source 1, pp.
11–12). A Macintosh manual scan is also linked below. The account therefore describes the
common game rather than comparing editions. Claims that depend on the manual's optional
settings are identified as such. Later *Deluxe* materials are linked for reference but are
not used to add mechanics to the original game.

## Role, objective, and game setup

The player is the president of a new railroad company. The task is to turn an initial
$1,000,000 of capital into a profitable, working railroad by choosing where to build,
which communities and industries to serve, what trains to run, and how to finance growth.
At the outset, $500,000 comes from investors buying company shares and $500,000 from
bonds. The company begins with no track, so the first connection and operating service
must be built from scratch. (Source 1, pp. 3–5, 95.)

Before play, the player chooses a world, a difficulty level, and three operating rules
called “reality levels.” The four worlds are Eastern USA, Western USA, England, and
Europe. They start in 1830, 1866, 1828, and 1900 respectively, and differ in geography,
available industries and cargo, and locomotive technology. Each world is a distinct map
and opportunity, not a separately scripted story. (Source 1, pp. 16–19; Source 3.)

Difficulty affects delivery revenue and the length of a president’s term. The manual gives
terms of 40 years for Investor, 60 for Financier, 80 for Mogul, and 100 for Tycoon. The
“reality” choices can be set independently:

- **No Collision Operation or Dispatcher Operation:** in the simpler mode, train conflicts do not produce collisions; in dispatcher operation, trains obey block signals and can collide.
- **Friendly or Cut-Throat Competition:** friendly rivals do not initiate takeovers or rate wars; cut-throat rivals may contest the player through the stock market and at stations.
- **Basic or Complex Economy:** basic economy makes moderate-sized cities broad markets for cargo; complex economy makes station demand depend on the nearby industries and population.

These settings are adjustable during a game. They let the player choose how much of the
railroad's operational, economic, and competitive simulation to manage. (Source 1, pp.
16–19, 26.)

The objective is to retire at the end of the chosen term with a strong company and a high
tycoon rating. The retirement report evaluates performance and may place an exceptional
player in the Railroader's Hall of Fame. The game can also end early if shareholders
remove the president or a rival takes control of the company. (Source 1, pp. 32–33.)

## Reading the world and controlling the game

The player works primarily through a map, menus, and reports. The map is tile-based and
has four zoom levels, each answering a different management question:

- The **Regional Display** shows the broad world and helps the player plan expansion.
- The **Area Display** is a schematic view of the railroad, emphasizing track, trains, stations, signals, and cargo reports without local terrain.
- The **Local Display** combines railroad features with nearby cities, industries, resources, and terrain, making it useful for finding routes and service opportunities.
- The **Detail Display** shows the map at its closest scale. It identifies individual map features and is where the player lays or removes track, builds stations and industries, and makes other local changes.

The change in map detail matters to play: the player can use a broad view to plan, a
schematic view to follow train traffic, and a close view to construct infrastructure.
Terrain and industry appear as map icons; trains are animated in the close view. The small
world-view inset indicates which part of the larger map is currently on screen. (Source 1,
pp. 20–31, 35–39.)

The interface is menu-driven and supports mouse and keyboard input. Menus provide
construction, actions, reports, and display options. The player can center or scroll the
map, zoom between displays, select an icon for more information, and use shortcut keys for
frequent tasks. A construction cursor marks the tile where an action will occur. Most game
information is also available in reports rather than being conveyed only by animation.

Shipping Reports summarize what each station can send and receive, what cargo is waiting,
and whether delivery rates are currently normal or altered. A Station Report gives more
exact supply and demand information and lists station improvements. Train Reports show
each train’s equipment, current status, route, planned stops, and cargo consist. Financial
and operational reports cover the balance sheet, income, train earnings, stock prices,
efficiency, accomplishments, and company history. The player can toggle shipping overlays
and a resource map on the Area and Local Displays, and can configure news messages and
event animations. (Source 1, pp. 21–31, 58–60, 65–68, 99–105.)

Time advances through months and years. Every two years the company closes an accounting
period and the player reviews its financial results; bond and short-term-loan interest is
charged annually. This periodic accounting makes expansion decisions consequential:
construction and equipment costs arrive before the additional service has had time to earn
revenue. (Source 1, pp. 28, 99–103.)

## Building a network

Track is laid one section at a time on the Detail Display, extending from existing track.
A section links neighboring map squares, allowing straight and angled routes and
junctions. Before committing to a route, the player can survey terrain to see relative
elevations. Grades and sharp curves slow trains; a direct route may therefore be less
profitable than one that costs more to build but permits faster service.

Water and mountains introduce special construction decisions. Rivers can require bridges,
and sufficiently difficult terrain can require tunnels. Ferries allow a route across
larger bodies of water, but trains travel slowly over them. Track may be double-tracked
later; this raises capacity on busy sections and reduces waiting, but costs money. Track
can also be demolished, returning some value for the right-of-way. These choices trade
initial capital against speed, capacity, and long-term operating performance. (Source 1,
pp. 49–55.)

Stations are required for trains to load and deliver cargo. Track passing a city or
industry does not by itself create service: the station is the transfer point, and its
economic radius determines which nearby population and industrial sites contribute supply
and demand. The player chooses among three station sizes—Depot, Station, and Terminal.
Larger stations cost more and cover a larger surrounding area. A station that is too small
may miss valuable sources or customers; one that is too large may spend money covering an
area with little traffic.

Signal Towers are a separate construction choice used to control train movements; they are
not one of the three station sizes. Stations include signaling, and the first station also
supplies an engine shop. Additional station improvements can support train building and
maintenance, speed consist changes, store cargo, or earn extra income from passenger
traffic. The player must decide whether an improvement’s cost is justified by the traffic
and operating needs at that location. (Source 1, pp. 56–62, 88–90.)

Industries and communities create linked cargo flows. Mines, farms, forests, cities, and
factories can supply particular cargoes or demand them; some industries transform inputs
into new products. For example, the tutorial follows coal delivered to a steel mill, steel
delivered to a factory, and manufactured goods carried back to a city. Connecting one
source to one destination can therefore create opportunities for further traffic, rather
than a single isolated delivery. The player can consult the resource map, station reports,
and the regional player-aid cards to find these relationships. The manual also describes
building new industries to encourage useful production near the railroad. (Source 1, pp.
81–87; Source 3.)

In the basic economy, a sufficiently large city accepts all cargo types. In the complex
economy, demand is determined by the city and industries within a station’s radius. The
complex setting makes destination selection and cargo planning more important: a train
must carry something a served station actually wants, and a station’s available cargo must
be matched to a destination that will pay for it. Supply can change as industries appear
or disappear, so a previously profitable route may need to be revised. (Source 1, pp.
16–17, 58–60, 83–87.)

## Trains, schedules, and deliveries

After building a station, the player can buy a locomotive at an engine shop and add cars
to make a train. The locomotive determines much of the train’s speed and hauling
capability; cars determine which passengers, mail, or freight it can carry. New locomotive
models become available over time. Choosing a fast engine for a short passenger service or
a stronger engine for heavy freight is one of the player’s recurring operating decisions.
The player can change a train’s locomotive and consist as the network and cargo mix
develop. (Source 1, pp. 63–75.)

For each train, the player sets a route as a sequence of station stops and can assign a
train class that affects how it follows that schedule. At stops, a train can deliver
cargo, pick up cargo, and change its consist to carry a different type on the next leg. A
train can be ordered to wait until full where collecting a complete load is worth the
delay. These controls allow one train to serve several linked industries and destinations,
but they require careful planning: an unsuitable car, missed stop, or cargo with no buyer
can leave equipment idle and revenue unrealized. (Source 1, pp. 72–79.)

Deliveries generate revenue for the railroad. Revenue depends on the cargo, the route and
service conditions; the chosen difficulty level also changes how much a delivery pays.
Station reports and train-income reports let the player compare supply, demand, waiting
cargo, and earnings. A priority shipment offers an additional objective and reward; it can
be handed from one train to another at a station, so a longer route can be served by
coordinated trains. (Source 1, pp. 81–87, 101–102.)

Signals divide track into blocks. In Dispatcher Operation, only one train may occupy a
single-track block at a time; signals prevent another train entering until the block is
clear. Double track increases the number of trains that can safely use a section. Players
can also pause trains or override a signal, but overriding removes the protection and can
cause a collision. In No Collision Operation, the player avoids this dispatcher-level
risk. (Source 1, pp. 88–93.)

Train wrecks destroy the train, its locomotive, cars, and cargo. The manual identifies
collisions and bridges washed out by floods as causes. In dispatcher play, signals and
track capacity are therefore active safety constraints rather than decorative
infrastructure. Choosing when to add double track, where to add signal control, and
whether to override a stop signal affects both throughput and risk. (Source 1, pp. 80,
88–93.)

## Company finance and competition

The railroad’s cash pays for track, right-of-way, bridges, stations, trains, cars, and
improvements. It also must cover recurring expenses such as interest and the maintenance
of trains, track, and stations. At each accounting period, income and financial reports
show whether revenue is covering operating costs and whether expansion is improving the
company’s position. A company can raise additional funds through bonds and short-term
borrowing, but debt carries interest and can become a burden if new construction fails to
produce enough traffic. Bankruptcy is possible and carries a lasting financing penalty.
(Source 1, pp. 95–103; Source 2.)

The stock market connects company growth to ownership and rivalry. The player can buy and
sell shares in their own railroad and in competing railroads, while rivals may buy shares
in the player’s company. A competitor that gains control can take over the company and end
the player’s presidency. Building into a new city may create an opportunity to issue
additional shares, while the stock and financial reports help the player monitor the value
and ownership of the competing companies. The broker interface is also where the player
directs any railroad they have taken control of. (Source 1, pp. 95–98, 102, 107–112.)

Rivals compete both financially and operationally. In cut-throat mode, a rival can start a
rate war at a station by contesting local service. The game awards local monopoly service
to the railroad that provides the better service; the loser must withdraw from that city
and loses its investment there. Rate wars can weaken an opponent or defend access to a
market, but reduced revenue makes them costly. Rivals also build networks and seek
profitable territory, so the player must watch both the map and stock reports rather than
optimizing only their own trains. Friendly mode removes these aggressive interactions.
(Source 1, pp. 107–112.)

## Retirement and success

The player may choose to retire before the term ends and see a report of current
retirement performance. Otherwise, the term ends at the limit associated with the selected
difficulty. The rating reflects the result of the company and the difficulty of the chosen
game, not simply the number of track sections built. Strong management means keeping a
profitable railroad operating while handling construction costs, service demand, train
capacity, maintenance, debt, and competitive pressure. Early removal by shareholders or
takeover by a rival is a failure state; successful retirement, and potentially entry in
the Hall of Fame, is the long-term goal. (Source 1, pp. 18–19, 32–33, 96.)

## Sources and version-specific references

Mechanics in this specification are chiefly based on the 1990 manual and its companion
material. The Macintosh and *Deluxe* edition-specific links (Sources 4–6) are included for
reference, but this specification does not import *Deluxe*-only features into the original
game's description. The periodical issues and secondary overview (Sources 7–10) are useful
for contemporary context and cross-checking the game's broad description; they are not
used in place of the manual for detailed rules.

1. [*Sid Meier's Railroad Tycoon* manual (1990), Internet Archive](https://archive.org/details/railroadtycoonmanual) — primary gameplay instructions.
2. [Technical Supplement (1990), Internet Archive](https://archive.org/download/railroadtycoonmanual/RailroadTycoon-TechnicalSupplement.pdf) — IBM PC setup, controls, and supplementary notes.
3. [Player Aid Cards and regional maps (1990), Internet Archive](https://archive.org/download/railroadtycoonmanual/RailroadTycoon-PlayerAidCards.pdf) — map icons and regional supply, demand, and cargo relationships.
4. [MicroProse *Railroad Tycoon* Macintosh materials, Internet Archive](https://archive.org/details/stx_Microprose_Railroad_Tycoon) — an archived Macintosh-platform scan.
5. [*Railroad Tycoon Deluxe* instruction manual (1993), Internet Archive](https://archive.org/details/railroad-tycoon-deluxe-instruction-manual) — separate later edition; linked for edition reference, not used as authority for original-game rules.
6. [*Railroad Tycoon Deluxe* player-aid card and regional maps (1993), Internet Archive](https://archive.org/details/rrtpacrmsoidupfhgsodfh) — separate later-edition reference.
7. [*Computer Gaming World*, issue 73, Internet Archive](https://archive.org/details/Computer_Gaming_World_Issue_73) — contemporary strategy and game coverage.
8. [*Computer Gaming World*, issue 74, Internet Archive](https://archive.org/details/Computer_Gaming_World_Issue_74) — contemporary game coverage.
9. [*Computer Gaming World*, issue 76, Internet Archive](https://archive.org/details/Computer_Gaming_World_Issue_76) — includes a *Railroad Tycoon* strategy article.
10. [“Railroad Tycoon (video game),” Wikipedia](https://en.wikipedia.org/wiki/Railroad_Tycoon_(video_game)) — secondary overview and citations to contemporary reviews and awards.
