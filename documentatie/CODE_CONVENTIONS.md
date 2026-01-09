# Code Conventions - WeedCraft Plugin

## Opmaak
- 4 spaties inspringing (geen tabs), max 120 karakters per regel
- Openende accolades op dezelfde regel, spaties rond operatoren
- Declareer altijd access modifiers expliciet

## Naamgeving
- **Klassen**: PascalCase (bijv. `WeedCropListener`, `PoliceManager`)
- **Methoden**: camelCase werkwoorden (bijv. `handleEvent()`, `getBalance()`)
- **Variabelen**: camelCase (bijv. `weedItems`, `activePlayers`)
- **Constanten**: UPPER_SNAKE_CASE (bijv. `MAX_WEED_LEVEL`)
- Listeners eindigen met `Listener`, managers met `Manager`

## Klassestructuur Volgorde
1. Constanten → 2. Statische variabelen → 3. Instantievelden → 4. Constructors → 5. Publieke methoden → 6. Private methoden

## Documentatie
- Javadoc voor alle publieke klassen en methoden met `@param`, `@return`, `@throws`
- Inline opmerkingen alleen voor complexe logica, leg uit waarom niet wat

## Foutafhandeling
- Vang specifieke uitzonderingen, nooit generieke `Exception`
- Controleer nulls voor bewerkingen, gebruik patroonherkenning: `if (sender instanceof Player player)`
- Log met passende niveaus: `Level.INFO`, `Level.WARNING`, `Level.SEVERE`

## Bukkit Specifieke Regels
- Gebruik `@EventHandler(ignoreCancelled = true)` voor event handlers
- Controleer altijd het zendertype voordat je cast in commando's
- Gebruik `ChatColor` voor gekleurde berichten, voeg `[WeedCraft]` vooraan toe
- Laad config in `onEnable()`, gebruik puntnotatie voor sleutels
