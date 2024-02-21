// Test Boxes v1 by 51mayday
// Adapts code from CommandLeo's getallitems.sc
// Fills chests with boxes of items. Can produce either single item type boxes with x stacks each or mixed boxes with x stacks of each item type over all boxes. 
// Takes in coordinates of a line of chests to fill with the desired boxes
// Takes in a mode: file + filename, stackables, 64_stackables, 16_stackables, unstackables
// Optionally takes in a .txt filename with a list of itemtypes to put in the boxes. 
// If a provided itemtype is invalid, it will be replaced by a placeholder tropical_fish renamed to the provided itemtype. 

global_options = {'all' -> [1, 16, 64], 'stackables' -> [16, 64], '64_stackables' -> [64], '16_stackables' -> [16], 'unstackables' -> [1]};

game_version = system_info('game_major_target');
global_items = map(item_list(), [_, null]);
global_survival_unobtainables = [
    'bedrock',
    'suspicious_sand',
    'suspicious_gravel',
    'budding_amethyst',
    'petrified_oak_slab',
    'chorus_plant',
    'spawner', // for 1.19.2-
    'monster_spawner', // for 1.19.3+
    'farmland',
    ...filter(item_list(), _~'infested' != null),
    'reinforced_deepslate',
    'end_portal_frame',
    'command_block',
    'barrier',
    'light',
    'grass_path', // for 1.16-
    'dirt_path', // for 1.17+
    'repeating_command_block',
    'chain_command_block',
    'structure_void',
    'structure_block',
    'jigsaw',
    'bundle',
    ...filter(item_list(), _~'spawn_egg' != null),
    'player_head',
    'command_block_minecart',
    'knowledge_book',
    'debug_stick',
    'frogspawn'
];

if(game_version < 18, global_survival_unobtainables += 'spore_blossom');
if(game_version < 19, global_survival_unobtainables += 'sculk_sensor');
global_junk_items = ['filled_map', 'written_book', 'tipped_arrow', 'firework_star', 'firework_rocket', 'bee_nest'];

__config() -> {
    'commands' -> {
        'test <from_pos> <to_pos> <stacks_per_type> <option>' -> 'make_test_boxes',
        'results <from_pos> <to_pos> <stacks_per_type> <option>' -> 'make_results_boxes',
        'test <from_pos> <to_pos> <stacks_per_type> file <filenames> ' -> 'make_test_boxes_file',
        'results <from_pos> <to_pos> <stacks_per_type> file <filenames> ' -> 'make_results_boxes_file'
    },
    'arguments' -> {
        'option' -> {
            'type' -> 'term',
            'options' -> keys(global_options)
        },
        'stacks_per_type' -> {
            'type' -> 'int',
            'min' -> 1,
        },
        'filenames' -> {
            'type' -> 'text',
            'suggester' -> _(args) -> (
                input = args:'filenames';
                entries = split('\\s+', input);
                item_lists = map(list_files('item_lists', 'shared_text'), slice(_, length('filenames') + 2));
                if(entries && slice(input, -1) != ' ', delete(entries, -1));
                return(if(entries, map(item_lists, str('%s %s', join(' ', entries), _)), item_lists));
            ),
            'case_sensitive' -> false
        },
        'pos_1' -> {
            'type' -> 'pos',
            'loaded' -> true
        },
        'pos_2' -> {
            'type' -> 'pos',
            'loaded' -> true
        }
    }
};

_scanStrip(from_pos, to_pos) -> (
    [x1, y1, z1] = from_pos;
    [x2, y2, z2] = to_pos;
    [dx, dy, dz] = map(to_pos - from_pos, if(_ < 0, -1, 1));

    if(
        x1 != x2,
            return(map(range(x1, x2 + dx, dx), block([_, y1, z1]))),
        y1 != y2,
            return(map(range(y1, y2 + dy, dy), block([x1, _, z1]))),
        z1 != z2,
            return(map(range(z1, z2 + dz, dz), block([x1, y1, _])))
    );
    return([block(from_pos)]);
);

_readItemList(item_list) -> (
    item_list_path = str('item_lists/%s', item_list);
    entries = map(read_file(item_list_path, 'shared_text'), [_, _:1 || null]);

    return(entries)
);

_isInvalidItem(item) -> (
    return(item_list()~item == null || (item == 'air'));
);

_get_item_list(option) -> (
    items = filter(global_items,
        [item, nbt] = _;
        global_options:option~stack_limit(item) != null && global_junk_items~item == null && item~'shulker_box' == null && item != 'air' && global_survival_unobtainables~item == null;
    );
);

_assemble_chests(pos1, pos2, raw_boxes) -> (
    print(str('Made %d boxes!', length(raw_boxes)));

    boxes = [];
    for(raw_boxes,
        boxItems = _;
        to_box = slice(boxItems, 0, if(length(boxItems) < 27, length(boxItems), 27));
        put(boxes, null, _make_box(to_box));
    );

    i = 0;
    affected_blocks = _scanStrip(pos1, pos2);
    while(length(boxes) > 0 && i < length(affected_blocks), length(boxes),
        block = affected_blocks:i;
        if(block != 'chest', i += 1; print('Skipping non-chest block!'); continue(););
        to_chest = slice(boxes, 0, if(length(boxes) < 54, length(boxes), 54));
        _fill_chest(block, to_chest);

        i += 1;
        loop(length(to_chest), delete(boxes, 0));
    );

    while(i < length(affected_blocks), length(affected_blocks), 
        block = affected_blocks:i;
        if(block != 'chest', i += 1; print('Skipping non-chest block!'); continue(););
        loop(inventory_size(block), inventory_set(block, _, 0););
        i += 1;
    );

    if(length(boxes) > 0, print(str('Not enough chests! Add %d more double chests.', floor(length(boxes)/54) + 1)), print('Finished successfully!'));
);

_fill_chest(chest, boxes) -> (
    loop(inventory_size(chest), 
        inventory_set(chest, _, 0);
        box = if(_ < length(boxes), boxes:_, null);

        inventory_set(chest, _, if(box != null, 1, 0), 'white_shulker_box', box);
    );
);

_make_box(boxItems) -> (
    nbt = '{BlockEntityTag:{Items:[';
    for(boxItems, 
        nbt += str('{Count:%db,Slot:%db,id:"minecraft:%s"%s}%s', stack_limit(_:0), _i, _:0, if(_:1 != null, str(',tag:%s', _:1), '');, if(_i < length(boxItems) - 1, ',', ''));
    );
    nbt += '],id:"minecraft:shulker_box"}}';
    return(nbt);
);

make_test_boxes(pos1, pos2, stacks_per_type, option) -> (
    items = _get_item_list(option);
    items = reduce(items, 
        item = _; 
        x = _a; 
        put(x, null, map(range(stacks_per_type), item), 'extend'); _a = x;
    , []);

    boxes = [];
    while(length(items) > 0, floor(length(items) / 27) + 1,
        box = []; 
        while(length(items) > 0 && _i < 27, 27, 
            i = floor(rand(length(items)));
            put(box, null, items:i);
            delete(items, i);
        );

        put(boxes, null, box);
    );

    _assemble_chests(pos1, pos2, boxes);
);

make_test_boxes_file(pos1, pos2, stacks_per_type, filenames) -> (
    item_lists = split(' ', filenames);
    items = [];
    put(items, null, reduce(item_lists, 
        list = _; 
        x = _a;
        put(x, null, _readItemList(list), 'extend');
        _a = x;
    , []), 'extend');

    items = map(items, if(_isInvalidItem(_:0), ['tropical_fish', str('{display:{Name:\'{"text":"%s"}\'}}', _:0)], [_:0, _:1]));
    
    items = reduce(items, 
        item = _; 
        x = _a; 
        put(x, null, map(range(stacks_per_type), item), 'extend'); _a = x;
    , []);

    boxes = [];
    while(length(items) > 0, floor(length(items) / 27) + 1,
        box = []; 
        while(length(items) > 0, 27,
            i = floor(rand(length(items)));
            put(box, null, items:i);
            delete(items, i);
        );

        put(boxes, null, box);
    );

    _assemble_chests(pos1, pos2, boxes);
);

make_results_boxes(pos1, pos2, stacks_per_type, option) -> (
    items = _get_item_list(option);

    boxes = [];
    for(items, 
        item = _;
        box = map(range(stacks_per_type), item);
        put(boxes, null, box);
    );

    _assemble_chests(pos1, pos2, boxes);
);

make_results_boxes_file(pos1, pos2, stacks_per_type, filenames) -> (
    item_lists = split(' ', filenames);
    items = [];

    put(items, null, reduce(item_lists, 
        list = _; 
        x = _a;
        put(x, null, _readItemList(list), 'extend');
        _a = x;
    , []), 'extend');

    items = map(items, if(_isInvalidItem(_:0), ['tropical_fish', str('{display:{Name:\'{"text":"%s"}\'}}', _:0)], [_:0, _:1]));

    boxes = [];
    for(items, 
        item = _;
        box = map(range(stacks_per_type), [item:0, item:1]);
        put(boxes, null, box);
    );

    _assemble_chests(pos1, pos2, boxes);
);