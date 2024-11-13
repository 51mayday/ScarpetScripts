// Inventory Analyzer v1.2 by 51 Mayday - for MC 1.21+
// Records data on items inside of inventories within a volume. It has two modes. 
// The first returns the total amounts of each item type (including contents of shulker boxes in inventories).
// The second returns the contents of each slot (including contents of shulker boxes in inventories).
// Returns results as .csv files and prints

// v1.2 changelog:
// Fixed double counting of double chest contents
// Updated to 1.21

__config() -> {
    'commands' -> {
        '<start_pos> <end_pos> <mode> <filename>' -> 'init',
        'slot_mode' -> _() -> print(player(), format(
                'wi  Records items (and their contents) slot by slot. Stores stackability type. Used to generate item lists for the Storage Tech Simulator.'
            )),
        'itemtype_mode' -> _() -> print(player(), format(
                'wi Returns the total amount of each item type within all inventories. Includes items nested in shulker boxes.'
            )),
        'debug' -> _() -> print(player(), global_checked);
    },
	'arguments' -> {
		'mode' -> {
			'type' -> 'string',
			'options' -> ['itemtypes', 'slots']
		},
        'filename' -> {
            'type' -> 'string'
        }
	},
    'scope'-> 'global'
};

init(startPos, endPos, countMode, filename) -> (
    global_checked = {};
    global_direction_map = {'north' -> {'left' -> 'east', 'right' -> 'west'}, 'south' -> {'left' -> 'west', 'right' -> 'east'}, 'east' -> {'left' -> 'south', 'right' -> 'north'}, 'west' -> {'left' -> 'north', 'right' -> 'south'}}; // Facing direction -> left/right -> direction of block to skip
    if(countMode == 'itemtypes', 
        print(player(), str('Counting items from [%d, %d, %d] to [%d, %d, %d].', startPos:0, startPos:1, startPos:2, endPos:0, endPos:1, endPos:2));
        countItems(startPos, endPos, filename);
    , 
        print(player(), str('Recording slots from [%d, %d, %d] to [%d, %d, %d].', startPos:0, startPos:1, startPos:2, endPos:0, endPos:1, endPos:2));
        countSlots(startPos, endPos, filename);
    );
);

countSlots(startPos, endPos, filename) -> (
    // goes slot by slot and stores the item found (and its contents if it's a box). Also records stackability type.
    
    slots = [];

    volume(startPos, endPos,
        inventory = _;
        if(has(global_checked, pos(inventory)), continue());
        if(size = inventory_size(inventory),
            for(range(size),
                slot_contents = inventory_get(inventory, _);
                if(slot_contents != null,
                    slot = {'itemtype' -> slot_contents:0, 'count' -> slot_contents:1, 'stackability' -> stack_limit(slot_contents:0), 'contents' -> []};
                    for(parse_nbt(slot_contents:2):'components':'minecraft:container',
                        boxItem = replace(_:'item':'id', 'minecraft:');
                        slot:'contents':length(slot:'contents') = {'itemtype' -> boxItem, 'count' -> _:'item':'count', 'stackability' -> stack_limit(boxItem), 'contents' -> []};
                    );
                    put(slots, null, slot);
                );
            );
            if(block(inventory) == block('chest') && block_state(inventory, 'type') != 'single', adjacentSkip = pos_offset(inventory, global_direction_map:block_state(inventory, 'facing'):block_state(inventory, 'type')); global_checked:adjacentSkip = 0); // if a chest, add the other side to the skip list
        );
    );

    return(csv(slots, filename););
);

countItems(startPos, endPos, filename) -> (
    // adds any item found in the inventories in the volume to a running total per item type. If the item is a shulker box, its contents are added to the list, as is the box itself

    item_count = {};

    volume(startPos, endPos, 
        inventory = _;
        if(has(global_checked, pos(inventory)), continue());
        if(size = inventory_size(inventory), 
            for(range(size),
                slot_contents = inventory_get(inventory, _);
                item = slot_contents:0;
                count = slot_contents:1;
                if(item != null, item_count:item += count, continue());
                for(parse_nbt(slot_contents:2):'components':'minecraft:container',
                    item_count:replace(_:'item':'id', 'minecraft:') += _:'item':'count';
                );
            );
            if(block(inventory) == block('chest') && block_state(inventory, 'type') != 'single', adjacentSkip = pos_offset(inventory, global_direction_map:block_state(inventory, 'facing'):block_state(inventory, 'type')); global_checked:adjacentSkip = 0); // if a chest, add the other side to the skip list
        );
    );

    return(csv(item_count, filename););
);

csv(list, filename) -> (
    // format and save as a .csv file
    // overwrites old file

    delete_file(str('%s.csv', filename), 'shared_any');

    if(type(list) == 'map',
        output = 'itemtype, count\n';
        for(list,
            output += str('%s, %d\n', _, list:_);
        );
    , if(type(list) == 'list',
        output = 'itemtype, count, stackability, contents: [contents, stackability, count, itemtype]\n';
        for(list,
            output += str('%s, %d, %d, %s\n', _:'itemtype', _:'count', _:'stackability', _:'contents');
        );
    ););

    print(player(), output);
    write_file(str('%s.csv', filename), 'shared_any', output);
    print(player(), format('wi Saved to: \\scripts\\shared\\' + filename + '.csv'));

    return(output);
);
