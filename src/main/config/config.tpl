# Feel free to edit. Mind all your own comments or white-spaces may be stripped by configuration save peformed by application.

update: ${update}
port: ${port}

# keep empty for system language or fill to force locale
# supported locales are cs, de, el, en, es, fr, hu, it, pl, ro, ru, sk
locale: ${locale}

score_display_duration: ${score_display_duration} # seconds
ai_place_tile_delay: ${ai_place_tile_delay} # miliseconds to wait before computer player place tile

beep_alert: ${beep_alert}
confirm: ${confirm}

players:
  # Colors as Java awt.Color constant or in hex-value. (third-party themes can ignore these colors)
  colors: ${colors}

  # You can declare default player names
  names: ${player_names}
  ai_names: ${ai_names}

# plugins/classic.jar - Graphics from original board game
# plugins/rgg_siege.jar - RGG's Siege tiles instead of original The Cathars tiles
plugins: ${plugins}

# possible expansions in profile definition:
#   WINTER, INNS_AND_CATHEDRALS, TRADERS_AND_BUILDERS, PRINCESS_AND_DRAGON,
#   TOWER, ABBEY_AND_MAYOR, BRIDGES_CASTLES_AND_BAZAARS, CATAPULT,
#   KING_AND_ROBBER_BARON, RIVER, RIVER_II, CATHARS, BESIEGERS, COUNT,
#   GQ11, CULT, TUNNEL, CORN_CIRCLES, PLAGUE, FESTIVAL, PHANTOM, WIND_ROSE,
#   GERMAN_MONASTERIES, FLIER, CORN_CIRCLES_II
#
# possible rules:
#   RANDOM_SEATING_ORDER,
#   TINY_CITY_2_POINTS, PRINCESS_MUST_REMOVE_KNIGHT,
#   DRAGON_MOVE_AFTER_SCORING, ESCAPE_RGG, PIG_HERD_ON_GQ_FARM,
#   MULTI_BARN_ALLOWEDD, TUNNELIZE_ALL_EXPANSIONS,  BAZAAR_NO_AUCTION, KEEP_CLOISTERS

presets: ${presets}

connection_history: ${connection_history}

${if hasDebug}
debug:
  # plain or zip
  save_format: ${save_format}

  autosave: ${autosave}

  # comment preset to disable autostart
  # use player name for human player or class for ai
  # - com.jcloisterzone.ai.legacyplayer.LegacyAiPlayer
  # - com.jcloisterzone.ai.DummyAiPlayer
  autostart: ${autostart}

  tile_definitions: ${tile_definitions}

  draw: ${draw}

  off_capabilities: ${off_capabilities}

  # area or figure
  area_highlight: ${area_highlight}
${end}

