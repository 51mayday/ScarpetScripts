// 51Waypoints v0 - by 51mayday
// Stores tp-able waypoints in a world
// Player can add and remove waypoints in-game

// on reload ->
global_waypoints = {}; // "waypoint name" -> [positions]
global_filename = replace(replace('51waypoints/51waypoints_' + str(system_info('world_name')), ' '), '\\.'); // based on world name: '51waypoints_worldname.json'

// get json file, 
    // if none exists: make one
    // else read to global_positions

if(null != list_files('', 'shared_folder') ~ '51waypoints', 
    if(null != list_files('51waypoints/', 'shared_json') ~ global_filename, 
        data = read_file(global_filename, 'shared_json');
        for(keys(data), 
            global_waypoints:_ = data:_;
        );
    ),
    write_file(global_filename, 'shared_json', global_waypoints);
);

__config() -> {
    'scope' -> 'global',
    'commands' -> {
        'list' -> ['refresh', 'list'],
        'add <name> <waypoint_pos>' -> 'add_waypoint',
        'remove <existing_waypoint>' -> 'remove_waypoint',
        'tp <existing_waypoint>' -> 'teleport'
    },
    'arguments' -> {
        'name' -> {
            'type' -> 'term',
            'suggest' -> []
        },
        'waypoint_pos' -> {
            'type' -> 'pos'
        },
        'existing_waypoint' -> {
            'type' -> 'term',
            'suggester' -> _(args) -> (keys(global_waypoints);)
        }
    }
};

write_changes() -> (
    // get json file,
        // if none exists: make one
        // else, save any changed global_positions (read in dict, check if key exists (if key exists, check if pos is the same), else set new key + pos) and rewrite to file
    if(null != list_files('', 'shared_folder') ~ '51waypoints', 
        if(null != list_files('51waypoints/', 'shared_json') ~  global_filename, 
            data = read_file(global_filename, 'shared_json');
            for(keys(global_waypoints), 
                if(null != keys(data) ~ _,
                    if(global_waypoints:_ != data:_, 
                        data:_ = global_waypoints:_;
                    );
                ,
                    data:_ = global_waypoints:_;
                );
            );
            // Delete removed waypoints
            for(keys(data),
                if(null == keys(global_waypoints) ~ _,
                    delete(data, _);
                );
            );
            if(data == null, data = global_waypoints);
        )
    , // else
        data = global_waypoints;
    );

    output = write_file(global_filename, 'shared_json', data);
    return(output)
);

read_changes() -> (
    // get json file,
        // if none exists: make one
        // else, save any changed global_positions (read in dict, check if key exists (if key exists, check if pos is the same), else set new key + pos) and rewrite to file
    if(null != list_files('', 'shared_folder') ~ '51waypoints', 
        if(null != list_files('51waypoints/', 'shared_json') ~ global_filename, 
            data = read_file(global_filename, 'shared_json');
            for(keys(data), 
                if(null != keys(global_waypoints) ~ _,
                    if(global_waypoints:_ != data:_, 
                        global_waypoints:_ = data:_; 
                    );
                ,
                    global_waypoints:_= data:_;
                );
            );
        )
    ,
        write_changes();
    );

    return(null)
);

add_waypoint(key, position) -> (
    global_waypoints:key = position;
    refresh('update');
    print(player(), str('Added waypoint %s: %f %f %f', key, ...position));
);

remove_waypoint(key) -> (
    print(player(), str('Removed waypoint %s: %f %f %f', key, ...global_waypoints:key));
    delete(global_waypoints, key);
    refresh('update');
    return(null)
);

refresh(option) -> (
    if(option == 'update' || option == 'both', write_changes());
    if(option == 'list' || option == 'read' || option == 'both', read_changes());
    if(option == 'list',
        for(keys(global_waypoints), 
            print(player(), str('%s: %f %f %f', _, ...global_waypoints:_));
        );
    );

    return(null)
);

teleport(key) -> (
    run(str('teleport %s %s %s', ...global_waypoints:key));
    return(null)
);

__on_close() -> (
    write_changes();
    return(null)
);