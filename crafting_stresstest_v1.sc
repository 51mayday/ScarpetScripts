// Crafting stress-tester for mod4-based mass-crafting systems
// By 51mayday, with help from Ners
// Assumes full stacks of items are delivered
// Assumes pistons are the selected recipe, using spruce planks

__config() -> {
    'commands' -> {
        '' -> _() -> print(player(), 'does nothing');,
        'start <origin_pos> <cycle>' -> 'start_tracker',
        'stop' -> 'stop_tracker',
        'status' -> 'status'
    },
    'arguments' -> {
        'cycle' -> {
            'type' -> 'int',
            'min' -> 10,
            'max' -> 54*15,
            'suggest' -> [18, 20, 22, 52]
        }
    },
    'scope' -> 'global'
};

reset_counter() -> (
    global_counter = {
        'status' -> false,
        'trials' -> 0,
        'fails' -> 0
    }
);

start_tracker(origin_pos, cycle) -> (
    if(global_counter:'status', print(player(), format('f » ', 'rb Testing already in progress. Please stop it first.')); return(););

    // else
    reset_counter();
    print(player(), str('Tracking for pistons starting at %d, %d, %d', origin_pos:0, origin_pos:1, origin_pos:2));
    global_counter:'status' = true;
    schedule(10, 'track', origin_pos, cycle);
);

stop_tracker() -> (
    print(player(), format(
    			'w Stopping mod4 crafting test',
    			'b \n» ',
    			'bl  Successes: ' + (global_counter:'trials' - global_counter:'fails') + ' batches crafted',
    			'rb  Fails: ' + global_counter:'fails'
    		));
    global_counter:'status' = false;
);

status() -> (
    if(global_counter:'status',
        print(player(), format(
                        'w Status for mod4 crafting test',
                        'b \n» ',
                        'bl  Successes: ' + (global_counter:'trials' - global_counter:'fails') + ' batches crafted',
                        'rb  Fails: ' + global_counter:'fails'
                    )
        );
    , // else
        print(player(), 'No test running')
    );
);

track(origin_pos, cycle) -> (
    sim_inventory = null;

    item_entities = entity_area('item', origin_pos, [1, 1, 1]);

    // kill item entities since we have already stored them
    for(item_entities, modify(_, 'remove'););

        if(length(item_entities) >=36 && global_counter:'status',
            global_counter:'trials' += 1;

            item_names = map(item_entities, get(_ ~ 'item', 0));

            sim_inventory = slice(item_names, 0, 36);
            loop(36, delete(item_names,0););

            while(length(item_names) || length(sim_inventory), 27*9,
                // count occurrences of cobblestone, spruce_planks, iron_ingots, and redstone in sim_inventory
                counter_cobblestone = reduce(sim_inventory, if(_ == 'cobblestone', _a +=1); _a, 0);
                counter_spruce_planks = reduce(sim_inventory, if(_ == 'spruce_planks', _a +=1); _a, 0);
                counter_iron_ingot = reduce(sim_inventory, if(_ == 'iron_ingot', _a +=1); _a, 0);
                counter_redstone = reduce(sim_inventory, if(_ == 'redstone', _a +=1); _a, 0);

                // if recipe success, remove 4 cobble, 4 planks, 1 iron, and 1 redstone from sim_inventory, then refill sim_inventory
                if(counter_cobblestone >= 4 && counter_spruce_planks >= 3 && counter_iron_ingot >= 1 && counter_redstone >= 1,
                    loop(4, first(sim_inventory, i = _i; _ == 'cobblestone'); delete(sim_inventory, i););
                    loop(3, first(sim_inventory, i = _i; _ == 'spruce_planks'); delete(sim_inventory, i););
                    first(sim_inventory, i = _i; _ == 'iron_ingot'); delete(sim_inventory, i);
                    first(sim_inventory, i = _i; _ == 'redstone'); delete(sim_inventory, i);

                    // refill sim_inventory from item_names
                    loop(9, put(sim_inventory, null, get(item_names, 0)); delete(item_names, 0););

                , // else
                    if(all([counter_cobblestone, counter_spruce_planks, counter_iron_ingot, counter_redstone], _ == 0), break());

                    // make reason
                    reason = '';
                    if(counter_cobblestone < 4,
                        reason = 'not enough cobble'; ex = true;
                    );
                    if(counter_spruce_planks < 3,
                        if(ex, reason += ', ';);
                        reason += 'not enough spruce planks'; ex = true;
                    );
                    if(counter_redstone < 1,
                        if(ex, reason += ', ';);
                        reason += 'not enough redstone'; ex = true;
                    );
                    if(counter_iron_ingot < 3,
                        if(ex, reason += ', ';);
                        reason += 'not enough iron'; ex = true;
                    );

                    global_counter:'fails' += 1;
                    print(player(), str('FAILED: %s', reason)); ex = false;

                    break();
                );
            );
        );

    if(global_counter:'status',
        schedule(cycle, 'track', origin_pos, cycle);
    );

);