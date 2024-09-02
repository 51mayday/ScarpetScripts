// Status Checker v0 - by 51mayday
// Checks if lamps at stored positions are on/off
// If not loaded, defaults to 'off'
// If loaded and lamp is on, returns 'machine x is on'
// Works in different dimensions
// Stores JSON dictionary: {'machine name' -> {'dim' -> dimension, 'pos' -> [posX posY posZ]}}
// Player can add/remove/list machines in-game
// Player can query all or one specific machine

// on reload ->
global_machines = {}; // {'machine name' -> {'dim' -> dimension, 'pos' -> [posX posY posZ]}}
global_filename = replace(replace('51status/51status_' + str(system_info('world_name')), ' '), '\\.'); // based on world name: '51status_worldname.json'

if(null != list_files('', 'shared_folder') ~ '51status', 
    if(null != list_files('51status/', 'shared_json') ~ global_filename, 
        data = read_file(global_filename, 'shared_json');
        for(keys(data), 
            global_machines:_ = data:_;
        );
    ),
    write_file(global_filename, 'shared_json', global_machines);
);

__config() -> {
    'scope' -> 'global',
    'commands' -> {
        'list' -> ['refresh', 'list'],
        'add <machine_name> <machine_dimension> <machine_pos>' -> 'add_machine',
        'remove <existing_machine_name>' -> 'remove_machine',
        'query <existing_machine_name>' -> 'query_machine',
        'query' -> 'query_all',
        'test' -> _() -> print(player(), global_machines)
    },
    'arguments' -> {
        'machine_name' -> {
            'type' -> 'term',
            'suggest' -> []
        },
        'machine_pos' -> {
            'type' -> 'pos'
        },
        'machine_dimension' -> {
            'type' -> 'dimension',
            'suggest' -> ['overworld', 'the_nether', 'the_end']
        },
        'existing_machine_name' -> {
            'type' -> 'term',
            'suggester' -> _(args) -> (keys(global_machines);)
        }
    }
};

write_changes() -> (
    // get json file,
        // if none exists: make one
        // else, save any changed global_positions (read in dict, check if key exists (if key exists, check if pos is the same), else set new key + pos) and rewrite to file
    if(null != list_files('', 'shared_folder') ~ '51status', 
        if(null != list_files('51status/', 'shared_json') ~  global_filename, 
            data = read_file(global_filename, 'shared_json');
            for(keys(global_machines), 
                if(null != keys(data) ~ _,
                    if(global_machines:_ != data:_, 
                        data:_ = global_machines:_;
                    );
                ,
                    data:_ = global_machines:_;
                );
            );
            // Delete removed waypoints
            for(keys(data),
                if(null == keys(global_machines) ~ _,
                    delete(data, _);
                );
            );
            if(data == null, data = global_machines);
        )
    , // else
        data = global_machines;
    );

    output = write_file(global_filename, 'shared_json', data);
    return(output)
);

read_changes() -> (
    // get json file,
        // if none exists: make one
        // else, save any changed global_positions (read in dict, check if key exists (if key exists, check if pos is the same), else set new key + pos) and rewrite to file
    if(null != list_files('', 'shared_folder') ~ '51status', 
        if(null != list_files('51status/', 'shared_json') ~ global_filename, 
            data = read_file(global_filename, 'shared_json');
            for(keys(data), 
                if(null != keys(global_machines) ~ _,
                    if(global_machines:_ != data:_, 
                        global_machines:_ = data:_; 
                    );
                ,
                    global_machines:_= data:_;
                );
            );
        )
    ,
        write_changes();
    );

    return(null)
);

add_machine(key, dimension, position) -> (
    global_machines:key = {'dim' -> dimension, 'pos' -> position};
    refresh('update');
    print(player(), str('Added machine %s: %s %f %f %f', key, dimension, ...position));
);

remove_machine(key) -> (
    print(player(), str('Removed machine %s: %s %f %f %f', key, global_machines:key:'dim', ...global_machines:key:'pos'));
    delete(global_machines, key);
    refresh('update');
    return(null)
);

refresh(option) -> (
    if(option == 'update' || option == 'both', write_changes());
    if(option == 'list' || option == 'read' || option == 'both', read_changes());
    if(option == 'list',
        for(keys(global_machines), 
            print(player(), str('%s: %s %f %f %f', _, global_machines:_:'dim', ...global_machines:_:'pos'));
        );
    );

    return(null);
);

query_machine(key) -> (
    // execute in minecraft:the_nether run script run if(block_state([-2, 33, -4], 'lit') == 'true', print('boob'))
    print(format(
        'wb Machine Status:'
    ));
    in_dimension(global_machines:key:'dim',
        if(loaded(global_machines:key:'pos'),
            if(block_state(global_machines:key:'pos', 'lit') == 'true', 
                print(format('w ' + key + ' is ', 'bl on'));,
            // else
                print(format('w ' + key + ' is ', 'br off'));;
            ),
        // else
            print(format('w ' + key + ' is ', 'br off'));
        );
    );
);

query_all() -> (
    print(format(
        'wb Machine Status:'
    ));
    for(keys(global_machines),
        key = _;
        in_dimension(global_machines:key:'dim',
        if(loaded(global_machines:key:'pos'),
            if(block_state(global_machines:key:'pos', 'lit') == 'true', 
                print(format('w ' + key + ' is ', 'bl on'));
            );
        );
        );
    );
);

__on_close() -> (
    write_changes();
);