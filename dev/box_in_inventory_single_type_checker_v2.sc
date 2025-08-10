// Box in Inventory Single Type Checker V2 for MC 1.21+
// Checks if each box passing through an inventory at x y z is single item type
// Marks checked boxes with nbt tag to avoid double counting
// Optional: set inventory in which to clear the already checked nbt tag
// Important: If using hoppers as the monitored inventory, take care! Some hopper setups are unreliable due to hopper hashing! The hopper selected for the script to monitor should be a hopper pushing into another inventory. If a hopper below the selected hopper can also pull items from it, the script will miss some boxes passing through.

__config() -> {
    'commands' -> {
        'start <origin_pos>' -> 'start_recording',
        'stop' -> 'stop_recording',
        'status' -> 'report_status',
        'help' -> 'print_help',
        'set_clean <clean_pos>' -> 'set_clean_pos'
    },
    'scope' -> 'player'
};

global_status = false;
global_cleaning = false;
global_counter = {'pass' -> 0, 'fail' -> {'empty' -> 0, 'mixed' -> 0, 'non-box' -> 0}};
global_boxes = item_list('c:shulker_boxes');

start_recording(origin_pos) -> (
    if(inventory_size(origin_pos) != null, 
        print(player(), str('Started monitoring boxes in the inventory at: [%d %d %d]', origin_pos));
        global_status = true;
        global_counter = {'pass' -> 0, 'fail' -> {'empty' -> 0, 'mixed' -> 0, 'non-box' -> 0}};
        schedule(1, 'record', origin_pos);
    , // else
        print(player(), 'Please select an actual inventory block');
    );
    
);

record(origin_pos) -> (
    if(!global_status, return(););
    if(inventory_has_items(origin_pos), 
        slots = reduce(range(inventory_size(origin_pos)), _a += inventory_get(origin_pos, _), []);
        for(slots,
            if(_ == null || _:2:'components':'minecraft:lore' ~ 'already_checked', continue(););
            box_type = '';
            slotData = _;
            slotIndex = _i;
            if(item_tags(_:0, 'c:shulker_boxes'), 
                if(slotData:2 == null || !has(parse_nbt(slotData:2):'components':'minecraft:container'), 
                    print(player(), 'Fail: empty box'); 
                    global_counter:'fail':'empty' = global_counter:'fail':'empty' + 1;
                    , // else
                    clean_box = true;
                    for(parse_nbt(slotData:2):'components':'minecraft:container',
                        item_type = _:'item':'id';
                        if(box_type == '', box_type = item_type);
                        if(box_type != item_type, clean_box = false; print(player(), 'Fail: mixed box'); global_counter:'fail':'mixed' = global_counter:'fail':'mixed' + 1; break(););
                    );
                    if(clean_box, global_counter:'pass' += 1;);
                );
                tag = nbt(slotData:2);
                put(tag, 'components.minecraft:lore', ['already_checked']);
                inventory_set(origin_pos, slotIndex, slotData:1, slotData:0, tag);
            , // else
                print(player(), 'Fail: non-box');
                tag = nbt(slotData:2);
                put(tag, 'components.minecraft:lore', ['already_checked']);
                inventory_set(origin_pos, slotIndex, slotData:1, slotData:0, tag);
                global_counter:'fail':'non-box' = global_counter:'fail':'non-box' + 1;
            );
        );
    );
    
    if(global_status, schedule(1, 'record', origin_pos));
);

set_clean_pos(clean_pos) -> (
    if(inventory_size(clean_pos) != null,
        print(player(), str('Started cleaning items in the inventory at: [%d %d %d]', clean_pos));
        global_cleaning = true;
        schedule(1, 'clean', clean_pos);
    , // else
        print(player(), 'Please select an actual inventory block');
    );
);

clean(clean_pos) -> (
    if(inventory_has_items(clean_pos),
        slots = reduce(range(inventory_size(clean_pos)), _a += inventory_get(clean_pos, _), []);
        for(slots,
            if(_ == null, continue(););
            if( _:2:'components':'minecraft:lore' ~ 'already_checked',
                tag = nbt(_:2);
                put(tag, 'components.minecraft:lore', ['cleaned']);
                inventory_set(clean_pos, _i, _:1, _:0, tag);
            )
        );
    );

    if(global_status && global_cleaning, schedule(1, 'clean', clean_pos));
);

stop_recording() -> (
    global_status = false;
    global_cleaning = false;
    print(player(), 'Stopped recording');
    print(player(), str('Pass: %d\nFail:\n Mixed: %d\n Empty: %d\n Non-box: %d', global_counter:'pass', global_counter:'fail':'mixed', global_counter:'fail':'empty', global_counter:'fail':'non-box'));
);

report_status() -> (
    print(player(), str('Pass: %d\nFail:\n Mixed: %d\n Empty: %d\n Non-box: %d', global_counter:'pass', global_counter:'fail':'mixed', global_counter:'fail':'empty', global_counter:'fail':'non-box'));
);

print_help() -> (
    print(player(), 'help');
);
