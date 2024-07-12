// Alignment Stresstest Monitor v2.5 - by 51mayday (Modified by Crec0)

// Tracks position and motion of items when they reaches given age
// In case of mismatched position or mismatched motion, it will count it as a failure and report the failed location and motion

global_epsilon = 0.000000001;
global_counter = {};

__config() -> {
    'commands' -> {
        'start <origin_pos> <range> <age> <item_chosen>' -> 'start_recording',
		'stop <from_chosen>' -> 'stop_recording',
		'status <from_chosen>' -> 'status',
		'help' -> 'print_help'
    },
	'arguments' -> {
		'range' -> {
			'type' -> 'int',
			'min' -> 4,
			'max' -> 128,
			'suggest' -> [16, 32, 64]
		},
		'age' -> {
			'type' -> 'int',
			'min' -> 1,
			'max' -> 6000,
			'suggest' -> [15, 30, 60]
		},
		'item_chosen' -> {
			'type' -> 'term',
			'suggest' -> map(item_list(), str(_));
		},
		'from_chosen' -> {
			'type' -> 'term',
			'suggester' -> _(args) -> (
				lst = keys(global_counter);
				put(lst, null, 'all', 'insert');
				lst
			)
		}
	},
    'scope'-> 'player'
};

print_help() -> (
	appname = system_info('app_name');
	print(format(
		'b Usage help for: ' + appname,
		'w \n',
		'w \n/' + appname + ' start <origin> <range> <age> <item>',
		'gi \n Starts tracking "item"\'s position and motion when it reaches the "age" for all "item"s in "range" from "origin"',
		'w \n/' + appname + ' stop <item>',
		'gi \n Stops tracking the "item". Prints status before exiting.',
		'w \n/' + appname + ' status <item>',
		'gi \n Prints status of the "item" tracker'
	))
);

reset_counters(item) -> (
	delete(global_counter:item)
);

status(item) -> (
	if (item == 'all',
		for(keys(global_counter), status(_));	
	, // else if
	 	global_counter:item:'enabled'
	, 
		print(format(
			'w Status for ', 
			'b ' + item, 
			'b \n» ', 
			'bq Total: ' + global_counter:item:'trials', 
			'bl  Success: ' + (global_counter:item:'trials' - global_counter:item:'fails'), 
			'rb  Fails: ' + global_counter:item:'fails'
		));
	, // else
		print(format('br Testing not in progress'));
	);
);

start_recording(spawn_pos, range, age, item) -> (
	if (global_counter:item:'enabled',
		print(format('f » ', 'rb Testing already in progress. Please stop it first.'));
		return();
	);
	reset_counters(item);

	global_counter:item = {
		'enabled' -> true,
		'trials' -> 0,
		'fails' -> 0,
		'has_recorded' -> false,
		'pos' -> null,
		'motion' -> null,
	};
	
	print(format(
		'f » ', 
		'g Alignment Tester testing: ', 
		'b ' + item, 
		'f \n» ', 
		'g Recording expected position and motion.', 
		'f \n» ', 
		'g Please start the aligner with ',
		'b ' + item,
		'g  feeding into it.'
	));
	record(spawn_pos, [range, range, range], age, item)
);

stop_recording(item) -> (
	if (item == 'all',
		for(keys(global_counter), stop_recording(_));
		return();
	);
	print(format(
		'f » ',
		'g Alignment Tester ', 
		'r stopped'
	));
	status(item);
	global_counter:item:'enabled' = false;
	reset_counters(item);
);

record(spawn_pos, range, age, item) -> (
	// Give tags to all matching items
	tag = player() + '_alignment_sample_' + item;
	for(filter(
			entity_area('item', spawn_pos, range), 
			_~'item':0 == item && !query(_, 'has_scoreboard_tag', tag)
		),
		modify(_, 'tag', tag);
	);

	sample_items = filter(
		entity_area('item', spawn_pos, range), 
		_ ~ 'age' == age && query(_, 'has_scoreboard_tag', tag)
	);

	if(global_counter:item:'has_recorded', 
		for(sample_items,
			item_id = _~'item':0;
			global_counter:item_id:'trials' = global_counter:item_id:'trials' + 1;

			is_pos_ok = all(_ ~ 'pos' - global_counter:item_id:'pos', abs(_) <= global_epsilon);
			is_motion_ok = all(_ ~ 'motion' - global_counter:item_id:'motion', abs(_) <= global_epsilon);

			if(!(is_pos_ok && is_motion_ok),
				global_counter:item_id:'fails' = global_counter:item_id:'fails' + 1;
				report(item_id, _);
			);
		);
	, // else
		for(sample_items,
			global_counter:item:'pos' = _ ~ 'pos';
			global_counter:item:'motion' = _ ~ 'motion';
			global_counter:item:'has_recorded' = true;
			print(format(
				'f » ', 
				'gi Starting test', 
				'f \n»', 
				'gi  Setting expected position: ', 
				str('!/tp @s %s %s %s', global_counter:item:'pos':0, global_counter:item:'pos':1, global_counter:item:'pos':2),
				'b ' + global_counter:item:'pos', 
				'f \n»', 
				'gi  Setting expected motion: ', 
				'b ' + global_counter:item:'motion'
			));
			break();
		);
	);
	
	if (global_counter:item:'enabled', 
		schedule(1, 'record', spawn_pos, range, age, item)
	);
);

report(item_id, item) -> (
	print(player(), str('%s failed at [%s, %s, %s] with motion [%s, %s, %s]', item_id, (item ~ 'pos'):0, (item ~ 'pos'):1, (item ~ 'pos'):2, (item ~ 'motion'):0, (item ~ 'motion'):1, (item ~ 'motion'):2));
);