// Geode Scanner v0.2.1_dev - by 51mayday and LazyPerfection

// Scans specified area (chunk coordinate to chunk coordinate) for budding amethyst blocks
// Returns chunk coordinates (x, z) of the afk position with the greatest number of budding amethyst blocks along with the number of exposed faces
// Used to verify results of external programs
// Written for Chronos's #geo-peri project
// Still a work in progress

__config() -> {
    'commands' -> {
        'start <originX> <originZ> <destinationX> <destinationZ>' -> 'start_counting',
		'here' -> 'start_here',
		'stop' -> 'stop_counting',
		'status' -> 'status',
		'help' -> 'print_help',
		'teleport <originX> <originZ> <destinationX> <destinationZ> <renderDistance>' -> 'start_teleport'
    },
	'arguments' -> {
	    'originX' -> {'type' -> 'int'},
	    'originZ' -> {'type' -> 'int'},
	    'destinationX' -> {'type' -> 'int'},
	    'destinationZ' -> {'type' -> 'int'},
	    'renderDistance' -> {'type' -> 'int'}
	},
    'scope'-> 'player'
};

global_started = false; global_afk_counting = false; global_teleporting = false;

print_help() -> (
	appname = system_info('app_name');
	print(format(
		'b Usage help for: ' + appname,
		'w \n',
		'w \n/' + appname + ' teleport <originX> <originY> <destinationX> <destinationY> <renderDistance>',
        'gi \n Teleports player from chunk "originX", "originZ" to chunk "destinationX", "destinationZ" to generate terrain',
        'w \n/' + appname + ' start <originX> <originY> <destinationX> <destinationY>',
        'gi \n Starts counting random ticked budding amethyst blocks in range of afk spots from chunk "originX", "originZ" to chunk "destinationX", "destinationZ"',
		'w \n/' + appname + ' here',
		'gi \n Starts counting random ticked budding amethyst blocks in range of afk spots within 5 chunks of the player position',
		'w \n/' + appname + ' stop',
		'gi \n Stops search',
		'w \n/' + appname + ' status',
		'gi \n Returns completion percentage for the stage the script is in (counting, evaluating afk positions, teleporting)'
	))
);

status() -> (
    if(global_started == true && global_afk_counting == false,
        print(format(
            'gi ' + floor((global_chunks_searched/global_total_chunks*100)*100)/100 + ' % of chunks searched',
        ));
    , if(global_afk_counting == true,
        print(format(
            'gi Checked ' + floor(global_counted/(length(global_xPossibilities)*length(global_zPossibilities)*length(global_cornerPossibilities))*100*100)/100 + ' % of AFK locations',
        ););
    , if(global_teleporting == true,
        print(format(
            'gi Teleported to ' + floor(global_chunks_teleported/global_chunks_to_teleport*100*100)/100 + ' % of chunks',
        ););
    ,
        print('Not running');
    );););
);

start_here() -> (
	closest_player = player();
    [posx, posy, posz] = query(closest_player, 'pos');
	
	chunkX = floor( posx / 16 );
	chunkZ = floor( posz / 16 );
	print(format('w Chunk Calculate [' + chunkX + ', ' + chunkZ + ']',));
	
	chunkXStart = chunkX - 5;
	chunkXEnd = chunkX + 5;
	chunkZStart = chunkZ - 5;
	chunkZEnd = chunkZ + 5;
	
	start_counting(chunkXStart, chunkZStart, chunkXEnd, chunkZEnd);
);

setup(originX, originZ, destinationX, destinationZ) -> (
    global_origin = [originX, originZ];
    global_destination = [destinationX, destinationZ];

    if(originX < destinationX, startX = originX - 8; endX = destinationX + 8;, startX = originX + 8; endX = destinationX - 8);
    if(originZ < destinationZ, startZ = originZ - 8; endZ = destinationZ + 8;, startZ = originZ + 8; endZ = destinationZ - 8);

    global_xRange = if(startX<endX, [range(startX, endX+1)], [range(endX, startX+1)]);
    global_zRange = if(startZ<endZ, [range(startZ, endZ+1)], [range(endZ, startZ+1)]);

    global_total_chunks = length(global_xRange)*length(global_zRange);

    if(startX > endX, temp = startX; startX = endX; endX = temp;);
    if(startZ > endZ, temp = startZ; startZ = endZ; endZ = temp;);

    return([startX, startZ, endX, endZ]);
);

start_counting(originX, originZ, destinationX, destinationZ) -> (
    if(global_started == false && global_afk_counting == false && global_teleporting == false, global_started = true; global_start_time = time();, print('Already running!'); return(););

    [startX, startZ, endX, endZ] = setup(originX, originZ, destinationX, destinationZ);

    global_chunks_searched = 0;

    print(format(
        'w Counting amethyst buds from chunk [' + startX + ', ' + startZ + '] to chunk ['+ endX + ', ' + endZ +']',
    ));

    global_counts = {... map(global_xRange, _ -> {... map(global_zRange, _ -> 0)})};

    schedule(1, 'scanner', 0, 0);
);

scanner(xIndex, zIndex) -> (
    if(global_started == false, print('Stopped'); return(););

    count = 0;

    volume([global_xRange:xIndex*16, -64, global_zRange:zIndex*16], [global_xRange:xIndex*16+15, 62, global_zRange:zIndex*16+15], if(_ == block('budding_amethyst'), count = count + 1));

    global_counts:(global_xRange:xIndex):(global_zRange:zIndex) = count;

    global_chunks_searched = global_chunks_searched + 1;

    if(zIndex + 1 == length(global_zRange), zIndex = 0; xIndex = xIndex + 1;, zIndex = zIndex + 1);

    if(xIndex == length(global_xRange),
        summa = 0;
        for(keys(global_counts), i = _; for(keys(global_counts:i), summa = summa + global_counts:i:_;););

        print(format(
            'w Found ' + summa + ' budding amethyst blocks in ' + global_chunks_searched + ' chunks! ',
            'gi (' + floor((time()-global_start_time)/1000/60*100)/100 + ' minutes)',
            'gi \nFinding optimal AFK spots...'
            ));

            schedule(1, 'start_finding_afk_spots');
            return();
    );

    schedule(1, 'scanner', xIndex, zIndex);
);

stop_counting() -> (
    global_started = false; global_afk_counting = false; global_teleporting = false;
);

start_finding_afk_spots() -> (
    global_afk_counting = true;
    global_xPossibilities = if(global_origin:0<global_destination:0, [range(global_origin:0, (global_destination:0)+1)], [range(global_destination:0, (global_origin:0)+1)]);
    global_zPossibilities = if(global_origin:1<global_destination:1, [range(global_origin:1, (global_destination:1)+1)], [range(global_destination:1, (global_origin:1)+1)]);
    global_cornerPossibilities = ['NW', 'NE', 'SE', 'SW'];

    global_top = [0];
    global_top_pos = [null];
    global_top_pos_relative = [null];

    xIndex = 0;
    yIndex = 0;
    cornerIndex = 0;

    schedule(1, 'find_afk_spots', xIndex, yIndex, cornerIndex);
);

global_corner_vectors = {
    'NW' -> [[-3, -8], [-2, -8], [-1, -8], [0, -8], [1, -8], [2, -8], [-5, -7], [-4, -7], [-3, -7], [-2, -7], [-1, -7], [0, -7], [1, -7], [2, -7], [3, -7], [4, -7], [-6, -6], [-5, -6], [-4, -6], [-3, -6], [-2, -6], [-1, -6], [0, -6], [1, -6], [2, -6], [3, -6], [4, -6], [5, -6], [-7, -5], [-6, -5], [-5, -5], [-4, -5], [-3, -5], [-2, -5], [-1, -5], [0, -5], [1, -5], [2, -5], [3, -5], [4, -5], [5, -5], [6, -5], [-7, -4], [-6, -4], [-5, -4], [-4, -4], [-3, -4], [-2, -4], [-1, -4], [0, -4], [1, -4], [2, -4], [3, -4], [4, -4], [5, -4], [6, -4], [-8, -3], [-7, -3], [-6, -3], [-5, -3], [-4, -3], [-3, -3], [-2, -3], [-1, -3], [0, -3], [1, -3], [2, -3], [3, -3], [4, -3], [5, -3], [6, -3], [7, -3], [-8, -2], [-7, -2], [-6, -2], [-5, -2], [-4, -2], [-3, -2], [-2, -2], [-1, -2], [0, -2], [1, -2], [2, -2], [3, -2], [4, -2], [5, -2], [6, -2], [7, -2], [-8, -1], [-7, -1], [-6, -1], [-5, -1], [-4, -1], [-3, -1], [-2, -1], [-1, -1], [0, -1], [1, -1], [2, -1], [3, -1], [4, -1], [5, -1], [6, -1], [7, -1], [-8, 0], [-7, 0], [-6, 0], [-5, 0], [-4, 0], [-3, 0], [-2, 0], [-1, 0], [0, 0], [1, 0], [2, 0], [3, 0], [4, 0], [5, 0], [6, 0], [7, 0], [-8, 1], [-7, 1], [-6, 1], [-5, 1], [-4, 1], [-3, 1], [-2, 1], [-1, 1], [0, 1], [1, 1], [2, 1], [3, 1], [4, 1], [5, 1], [6, 1], [7, 1], [-8, 2], [-7, 2], [-6, 2], [-5, 2], [-4, 2], [-3, 2], [-2, 2], [-1, 2], [0, 2], [1, 2], [2, 2], [3, 2], [4, 2], [5, 2], [6, 2], [7, 2], [-7, 3], [-6, 3], [-5, 3], [-4, 3], [-3, 3], [-2, 3], [-1, 3], [0, 3], [1, 3], [2, 3], [3, 3], [4, 3], [5, 3], [6, 3], [-7, 4], [-6, 4], [-5, 4], [-4, 4], [-3, 4], [-2, 4], [-1, 4], [0, 4], [1, 4], [2, 4], [3, 4], [4, 4], [5, 4], [6, 4], [-6, 5], [-5, 5], [-4, 5], [-3, 5], [-2, 5], [-1, 5], [0, 5], [1, 5], [2, 5], [3, 5], [4, 5], [5, 5], [-5, 6], [-4, 6], [-3, 6], [-2, 6], [-1, 6], [0, 6], [1, 6], [2, 6], [3, 6], [4, 6], [-3, 7], [-2, 7], [-1, 7], [0, 7], [1, 7], [2, 7]],
    'NE' -> [[-2, -8], [-1, -8], [0, -8], [1, -8], [2, -8], [3, -8], [-4, -7], [-3, -7], [-2, -7], [-1, -7], [0, -7], [1, -7], [2, -7], [3, -7], [4, -7], [5, -7], [-5, -6], [-4, -6], [-3, -6], [-2, -6], [-1, -6], [0, -6], [1, -6], [2, -6], [3, -6], [4, -6], [5, -6], [6, -6], [-6, -5], [-5, -5], [-4, -5], [-3, -5], [-2, -5], [-1, -5], [0, -5], [1, -5], [2, -5], [3, -5], [4, -5], [5, -5], [6, -5], [7, -5], [-6, -4], [-5, -4], [-4, -4], [-3, -4], [-2, -4], [-1, -4], [0, -4], [1, -4], [2, -4], [3, -4], [4, -4], [5, -4], [6, -4], [7, -4], [-7, -3], [-6, -3], [-5, -3], [-4, -3], [-3, -3], [-2, -3], [-1, -3], [0, -3], [1, -3], [2, -3], [3, -3], [4, -3], [5, -3], [6, -3], [7, -3], [8, -3], [-7, -2], [-6, -2], [-5, -2], [-4, -2], [-3, -2], [-2, -2], [-1, -2], [0, -2], [1, -2], [2, -2], [3, -2], [4, -2], [5, -2], [6, -2], [7, -2], [8, -2], [-7, -1], [-6, -1], [-5, -1], [-4, -1], [-3, -1], [-2, -1], [-1, -1], [0, -1], [1, -1], [2, -1], [3, -1], [4, -1], [5, -1], [6, -1], [7, -1], [8, -1], [-7, 0], [-6, 0], [-5, 0], [-4, 0], [-3, 0], [-2, 0], [-1, 0], [0, 0], [1, 0], [2, 0], [3, 0], [4, 0], [5, 0], [6, 0], [7, 0], [8, 0], [-7, 1], [-6, 1], [-5, 1], [-4, 1], [-3, 1], [-2, 1], [-1, 1], [0, 1], [1, 1], [2, 1], [3, 1], [4, 1], [5, 1], [6, 1], [7, 1], [8, 1], [-7, 2], [-6, 2], [-5, 2], [-4, 2], [-3, 2], [-2, 2], [-1, 2], [0, 2], [1, 2], [2, 2], [3, 2], [4, 2], [5, 2], [6, 2], [7, 2], [8, 2], [-6, 3], [-5, 3], [-4, 3], [-3, 3], [-2, 3], [-1, 3], [0, 3], [1, 3], [2, 3], [3, 3], [4, 3], [5, 3], [6, 3], [7, 3], [-6, 4], [-5, 4], [-4, 4], [-3, 4], [-2, 4], [-1, 4], [0, 4], [1, 4], [2, 4], [3, 4], [4, 4], [5, 4], [6, 4], [7, 4], [-5, 5], [-4, 5], [-3, 5], [-2, 5], [-1, 5], [0, 5], [1, 5], [2, 5], [3, 5], [4, 5], [5, 5], [6, 5], [-4, 6], [-3, 6], [-2, 6], [-1, 6], [0, 6], [1, 6], [2, 6], [3, 6], [4, 6], [5, 6], [-2, 7], [-1, 7], [0, 7], [1, 7], [2, 7], [3, 7]],
    'SE' -> [[-2, -7], [-1, -7], [0, -7], [1, -7], [2, -7], [3, -7], [-4, -6], [-3, -6], [-2, -6], [-1, -6], [0, -6], [1, -6], [2, -6], [3, -6], [4, -6], [5, -6], [-5, -5], [-4, -5], [-3, -5], [-2, -5], [-1, -5], [0, -5], [1, -5], [2, -5], [3, -5], [4, -5], [5, -5], [6, -5], [-6, -4], [-5, -4], [-4, -4], [-3, -4], [-2, -4], [-1, -4], [0, -4], [1, -4], [2, -4], [3, -4], [4, -4], [5, -4], [6, -4], [7, -4], [-6, -3], [-5, -3], [-4, -3], [-3, -3], [-2, -3], [-1, -3], [0, -3], [1, -3], [2, -3], [3, -3], [4, -3], [5, -3], [6, -3], [7, -3], [-7, -2], [-6, -2], [-5, -2], [-4, -2], [-3, -2], [-2, -2], [-1, -2], [0, -2], [1, -2], [2, -2], [3, -2], [4, -2], [5, -2], [6, -2], [7, -2], [8, -2], [-7, -1], [-6, -1], [-5, -1], [-4, -1], [-3, -1], [-2, -1], [-1, -1], [0, -1], [1, -1], [2, -1], [3, -1], [4, -1], [5, -1], [6, -1], [7, -1], [8, -1], [-7, 0], [-6, 0], [-5, 0], [-4, 0], [-3, 0], [-2, 0], [-1, 0], [0, 0], [1, 0], [2, 0], [3, 0], [4, 0], [5, 0], [6, 0], [7, 0], [8, 0], [-7, 1], [-6, 1], [-5, 1], [-4, 1], [-3, 1], [-2, 1], [-1, 1], [0, 1], [1, 1], [2, 1], [3, 1], [4, 1], [5, 1], [6, 1], [7, 1], [8, 1], [-7, 2], [-6, 2], [-5, 2], [-4, 2], [-3, 2], [-2, 2], [-1, 2], [0, 2], [1, 2], [2, 2], [3, 2], [4, 2], [5, 2], [6, 2], [7, 2], [8, 2], [-7, 3], [-6, 3], [-5, 3], [-4, 3], [-3, 3], [-2, 3], [-1, 3], [0, 3], [1, 3], [2, 3], [3, 3], [4, 3], [5, 3], [6, 3], [7, 3], [8, 3], [-6, 4], [-5, 4], [-4, 4], [-3, 4], [-2, 4], [-1, 4], [0, 4], [1, 4], [2, 4], [3, 4], [4, 4], [5, 4], [6, 4], [7, 4], [-6, 5], [-5, 5], [-4, 5], [-3, 5], [-2, 5], [-1, 5], [0, 5], [1, 5], [2, 5], [3, 5], [4, 5], [5, 5], [6, 5], [7, 5], [-5, 6], [-4, 6], [-3, 6], [-2, 6], [-1, 6], [0, 6], [1, 6], [2, 6], [3, 6], [4, 6], [5, 6], [6, 6], [-4, 7], [-3, 7], [-2, 7], [-1, 7], [0, 7], [1, 7], [2, 7], [3, 7], [4, 7], [5, 7], [-2, 8], [-1, 8], [0, 8], [1, 8], [2, 8], [3, 8]],
    'SW' -> [[-3, -7], [-2, -7], [-1, -7], [0, -7], [1, -7], [2, -7], [-5, -6], [-4, -6], [-3, -6], [-2, -6], [-1, -6], [0, -6], [1, -6], [2, -6], [3, -6], [4, -6], [-6, -5], [-5, -5], [-4, -5], [-3, -5], [-2, -5], [-1, -5], [0, -5], [1, -5], [2, -5], [3, -5], [4, -5], [5, -5], [-7, -4], [-6, -4], [-5, -4], [-4, -4], [-3, -4], [-2, -4], [-1, -4], [0, -4], [1, -4], [2, -4], [3, -4], [4, -4], [5, -4], [6, -4], [-7, -3], [-6, -3], [-5, -3], [-4, -3], [-3, -3], [-2, -3], [-1, -3], [0, -3], [1, -3], [2, -3], [3, -3], [4, -3], [5, -3], [6, -3], [-8, -2], [-7, -2], [-6, -2], [-5, -2], [-4, -2], [-3, -2], [-2, -2], [-1, -2], [0, -2], [1, -2], [2, -2], [3, -2], [4, -2], [5, -2], [6, -2], [7, -2], [-8, -1], [-7, -1], [-6, -1], [-5, -1], [-4, -1], [-3, -1], [-2, -1], [-1, -1], [0, -1], [1, -1], [2, -1], [3, -1], [4, -1], [5, -1], [6, -1], [7, -1], [-8, 0], [-7, 0], [-6, 0], [-5, 0], [-4, 0], [-3, 0], [-2, 0], [-1, 0], [0, 0], [1, 0], [2, 0], [3, 0], [4, 0], [5, 0], [6, 0], [7, 0], [-8, 1], [-7, 1], [-6, 1], [-5, 1], [-4, 1], [-3, 1], [-2, 1], [-1, 1], [0, 1], [1, 1], [2, 1], [3, 1], [4, 1], [5, 1], [6, 1], [7, 1], [-8, 2], [-7, 2], [-6, 2], [-5, 2], [-4, 2], [-3, 2], [-2, 2], [-1, 2], [0, 2], [1, 2], [2, 2], [3, 2], [4, 2], [5, 2], [6, 2], [7, 2], [-8, 3], [-7, 3], [-6, 3], [-5, 3], [-4, 3], [-3, 3], [-2, 3], [-1, 3], [0, 3], [1, 3], [2, 3], [3, 3], [4, 3], [5, 3], [6, 3], [7, 3], [-7, 4], [-6, 4], [-5, 4], [-4, 4], [-3, 4], [-2, 4], [-1, 4], [0, 4], [1, 4], [2, 4], [3, 4], [4, 4], [5, 4], [6, 4], [-7, 5], [-6, 5], [-5, 5], [-4, 5], [-3, 5], [-2, 5], [-1, 5], [0, 5], [1, 5], [2, 5], [3, 5], [4, 5], [5, 5], [6, 5], [-6, 6], [-5, 6], [-4, 6], [-3, 6], [-2, 6], [-1, 6], [0, 6], [1, 6], [2, 6], [3, 6], [4, 6], [5, 6], [-5, 7], [-4, 7], [-3, 7], [-2, 7], [-1, 7], [0, 7], [1, 7], [2, 7], [3, 7], [4, 7], [-3, 8], [-2, 8], [-1, 8], [0, 8], [1, 8], [2, 8]]
};

find_afk_spots(xIndex, zIndex, cornerIndex) -> (
    if(global_afk_counting == false, print('Stopped!'); return(););

    xCandidate = global_xPossibilities:xIndex;
    zCandidate = global_zPossibilities:zIndex;
    cornerCandidate = global_cornerPossibilities:cornerIndex;

    count = 0;
    for(global_corner_vectors:cornerCandidate,
        count = count + global_counts:(xCandidate + _:0):(zCandidate + _:1);
    );

    if(first(global_top, rankingIndex = _i; count>_) != null;,
        put(global_top, rankingIndex, count, 'insert');
        put(global_top_pos, rankingIndex, [xCandidate, zCandidate, cornerCandidate], 'insert');
    );

    if(length(global_top) > 5,
        delete(global_top, -1);
        delete(global_top_pos, -1);
    );

    cornerIndex = cornerIndex + 1;

    global_counted = global_counted + 1;

    if(cornerCandidate == 'SW',
        zIndex = zIndex + 1;
        if(zIndex >= length(global_zPossibilities), zIndex = 0; xIndex = xIndex + 1;);
    );

    if(xIndex == length(global_xPossibilities),
        // count faces for top 5
        top_faces = [null];
        for(global_top_pos,
            top_faces:_i = face_count(_);
        );

        print(format(
            'gi (' + floor((time() - global_start_time)/1000/60*100)/100 + ' minutes)'
        ););

        print('Top 5 candidates (chunk coordinates and cardinal chunk corner):');

        for(global_top,
            if(_ > 0,
                if(global_top_pos:_i:2 == 'NW',
                    tp_loc = [global_top_pos:_i:0*16, global_top_pos:_i:1*16];
                , if(global_top_pos:_i:2 == 'NE',
                    tp_loc = [global_top_pos:_i:0*16 + 15, global_top_pos:_i:1*16];
                , if(global_top_pos:_i:2 == 'SW',
                    tp_loc = [global_top_pos:_i:0*16, global_top_pos:_i:1*16 + 15];
                , if(global_top_pos:_i:2 == 'SE',
                    tp_loc = [global_top_pos:_i:0*16 + 15, global_top_pos:_i:1*16 + 15];
                ););););

                print(format(
                    'w ', (_i+1), '. Found ', _, ' budding amethyst blocks at: ', 'wb ' + global_top_pos:_i, str('^gi /teleport @s %d 100 %d', tp_loc), str('!/teleport @s %d 100 %d', tp_loc), 'w  with ' + top_faces:_i + ' exposed faces',
                ););
            );
        );

        global_started = false; global_afk_counting = false;

        return();
    );
    schedule(1, 'find_afk_spots', xIndex, zIndex, cornerIndex);
);

face_count(position_vector) -> (
    xPos = position_vector:0;
    zPos = position_vector:1;
    cornerPos = position_vector:2;

    count = 0;
    for(global_corner_vectors:cornerPos,
        count = count + face_counter(xPos + _:0, zPos + _:1);
    );

    return(count);
);

face_counter(xPos_shift, zPos_shift) -> (
    count = 0;
    volume([xPos_shift*16, -64, zPos_shift*16], [xPos_shift*16+15, 62, zPos_shift*16+15],
        if(_ == block('budding_amethyst'), count = count + for(neighbours(_x, _y, _z), _ != 'budding_amethyst';););
    );

    return(count);
);

start_teleport(originX, originZ, destinationX, destinationZ, renderDistance) -> (
    if(global_started == false && global_afk_counting == false && global_teleporting == false, global_teleporting = true; print(format('gi Generating terrain...')); global_teleport_start_time = time();, print('Already running!'); return(););

    [startX, startZ, endX, endZ] = setup(originX, originZ, destinationX, destinationZ);

    anchor = [startX, startZ, endX, endZ];

    global_chunks_teleported = 0;

    global_fin_pos = query(player(), 'pos');

    increment = floor(renderDistance*sqrt(2));

    global_chunks_to_teleport = (floor((endX-startX)/increment)+1)*(floor((endZ-startZ)/increment)+1);

    schedule(1, 'teleport', startX, startZ, increment, anchor);
);

teleport(xPos, zPos, increment, anchor) -> (
    if(global_teleporting == false, print('Stopped teleporting'); run(str('teleport @s %d %d %d', global_fin_pos)); return(););

        run(str('teleport @s %d 100 %d', xPos*16, zPos*16));

        global_chunks_teleported = global_chunks_teleported + 1;

        if(zPos >= anchor:3, zPos = anchor:1; xPos = xPos + increment;, zPos = zPos + increment);

        if(xPos >= anchor:2 || global_chunks_teleported == global_chunks_to_teleport + 1,
            print(format(
                'gi Done teleporting in ' + floor((time()-global_teleport_start_time)/1000/60*100)/100 + ' minutes!',
                ));

                run(str('teleport @s %d %d %d', global_fin_pos));

                global_teleporting = false;

                return();
        );

        // change the first value to however long it takes your PC to generate new terrain around you
        schedule(100, 'teleport', xPos, zPos, increment, anchor);
);