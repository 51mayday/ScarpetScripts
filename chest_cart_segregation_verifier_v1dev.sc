// Chest Cart Segregation Verifier V1_dev
// Checks if each chest cart passing through some position x y z is full of single item type boxes all of the same item type
// Kills chest carts automatically after scanning, logs total statistics

global_counter = {'pass' -> 0, 'fail' -> 0};

__config() -> {
    'commands' -> {
        'start <origin_pos>' -> 'start_recording',
        'stop' -> 'stop_recording',
        'status' -> 'report_status',
        'help' -> 'print_help'
    },
    'arguments' -> {
        'range' -> {
			'type' -> 'int',
			'min' -> 1,
			'max' -> 20,
			'suggest' -> [1, 2, 5]
        }
    },
    'scope' -> 'player'
};

global_status = false;
global_counter = {'pass' -> 0, 'fail' -> 0};

start_recording(origin_pos) -> (
    print(player(), str('Started monitoring chest carts at: [%d %d %d]', origin_pos));
    global_status = true;
    global_counter = {'pass' -> 0, 'fail' -> 0};
    schedule(1, 'record', origin_pos);
);

record(origin_pos) -> (
    chest_cart = true;
    hopper_cart = true;

    if(!global_status, return(););

    carts = [if(chest_cart, ...entity_area('chest_minecart', origin_pos, [0.5, 0.5, 0.5])), if(hopper_cart, ...entity_area('hopper_minecart', origin_pos, [0.5, 0.5, 0.5]))];
    sample_carts = filter(carts, and(_ != null, !query(_, 'has_scoreboard_tag', 'already_checked')));
    // print(player(), sample_carts);

    for(sample_carts, 
        modify(_, 'tag', 'already_checked');
        inventory = parse_nbt(query(_, 'nbt', 'Items'));
        if(length(inventory) == 0, print(player(), 'Fail: empty cart'); global_counter:'fail' += 1; continue(););
        cart_type = '';
        for(inventory, 
            box_type = '';
            if(_:'tag':'BlockEntityTag':'id' == 'minecraft:shulker_box', 
                for(_:'tag':'BlockEntityTag':'Items',
                    item_type = _:'id';
                    if(box_type == '', box_type = item_type);
                    if(box_type != item_type, print(player(), 'Fail: mixed box'); global_counter:'fail' += 1; break(););  
                    if(cart_type == '', cart_type = item_type);
                    if(cart_type != item_type, print(player(), 'Fail: mixed cart'); global_counter:'fail' += 1; break(););  
                );
                global_counter:'pass' += 1;
            , // else
                print(player(), 'Fail: non-box');
                global_counter:'fail' += 1;
                break();
            );
        );
    );
    
    if(global_status, schedule(1, 'record', origin_pos));
);

stop_recording() -> (
    global_status = false;
    print(player(), 'Stopped recording');
    print(player(), str('Pass: %d\nFail: %d', global_counter:'pass', global_counter:'fail'));
);

report_status() -> (
    print(player(), global_counter);
);

print_help() -> (
    print(player(), 'help');
);


//[{tag: {BlockEntityTag: {Items: [{Slot: 0, Count: 64, id: minecraft:white_stained_glass}, {Slot: 1, Count: 64, id: minecraft:white_stained_glass}, {Slot: 2, Count: 64, id: minecraft:white_stained_glass}, {Slot: 3, Count: 64, id: minecraft:white_stained_glass}, {Slot: 4, Count: 64, id: minecraft:white_stained_glass}, {Slot: 5, Count: 64, id: minecraft:white_stained_glass}, {Slot: 6, Count: 64, id: minecraft:white_stained_glass}, {Slot: 7, Count: 64, id: minecraft:white_stained_glass}, {Slot: 8, Count: 64, id: minecraft:white_stained_glass}, {Slot: 9, Count: 64, id: minecraft:white_stained_glass}, {Slot: 10, Count: 64, id: minecraft:white_stained_glass}, {Slot: 11, Count: 64, id: minecraft:white_stained_glass}, {Slot: 12, Count: 64, id: minecraft:white_stained_glass}, {Slot: 13, Count: 64, id: minecraft:white_stained_glass}, {Slot: 14, Count: 64, id: minecraft:white_stained_glass}, {Slot: 15, Count: 64, id: minecraft:white_stained_glass}, {Slot: 16, Count: 64, id: minecraft:white_stained_glass}, {Slot: 17, Count: 64, id: minecraft:white_stained_glass}, {Slot: 18, Count: 64, id: minecraft:white_stained_glass}, {Slot: 19, Count: 64, id: minecraft:white_stained_glass}, {Slot: 20, Count: 64, id: minecraft:white_stained_glass}, {Slot: 21, Count: 64, id: minecraft:white_stained_glass}, {Slot: 22, Count: 64, id: minecraft:white_stained_glass}, {Slot: 23, Count: 64, id: minecraft:white_stained_glass}, {Slot: 24, Count: 64, id: minecraft:white_stained_glass}, {Slot: 25, Count: 64, id: minecraft:white_stained_glass}, {Slot: 26, Count: 64, id: minecraft:white_stained_glass}], id: minecraft:shulker_box}}, Slot: 0, Count: 1, id: minecraft:white_shulker_box}, {tag: {BlockEntityTag: {Items: [{Slot: 0, Count: 64, id: minecraft:white_stained_glass}, {Slot: 1, Count: 64, id: minecraft:white_stained_glass}, {Slot: 2, Count: 64, id: minecraft:white_stained_glass}, {Slot: 3, Count: 64, id: minecraft:white_stained_glass}, {Slot: 4, Count: 64, id: minecraft:white_stained_glass}, {Slot: 5, Count: 64, id: minecraft:white_stained_glass}, {Slot: 6, Count: 64, id: minecraft:white_stained_glass}, {Slot: 7, Count: 64, id: minecraft:white_stained_glass}, {Slot: 8, Count: 64, id: minecraft:white_stained_glass}, {Slot: 9, Count: 64, id: minecraft:white_stained_glass}, {Slot: 10, Count: 64, id: minecraft:white_stained_glass}, {Slot: 11, Count: 64, id: minecraft:white_stained_glass}, {Slot: 12, Count: 64, id: minecraft:white_stained_glass}, {Slot: 13, Count: 64, id: minecraft:white_stained_glass}, {Slot: 14, Count: 64, id: minecraft:white_stained_glass}, {Slot: 15, Count: 64, id: minecraft:white_stained_glass}, {Slot: 16, Count: 64, id: minecraft:white_stained_glass}, {Slot: 17, Count: 64, id: minecraft:white_stained_glass}, {Slot: 18, Count: 64, id: minecraft:white_stained_glass}, {Slot: 19, Count: 64, id: minecraft:white_stained_glass}, {Slot: 20, Count: 64, id: minecraft:white_stained_glass}, {Slot: 21, Count: 64, id: minecraft:white_stained_glass}, {Slot: 22, Count: 64, id: minecraft:white_stained_glass}, {Slot: 23, Count: 64, id: minecraft:white_stained_glass}, {Slot: 24, Count: 64, id: minecraft:white_stained_glass}, {Slot: 25, Count: 64, id: minecraft:white_stained_glass}, {Slot: 26, Count: 64, id: minecraft:white_stained_glass}], id: minecraft:shulker_box}}, Slot: 1, Count: 1, id: minecraft:white_shulker_box}]