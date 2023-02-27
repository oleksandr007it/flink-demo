create table if not exists bet_slip
(
	id bigint  not null
		primary key,
	created_by varchar(32) null,
	created_date datetime(6) not null,
	last_modified_by varchar(32) null,
	last_modified_date datetime(6) null,
	game_code varchar(255) null,
	init_wager_amount decimal(19,2) null,
	odds decimal(19,2) null,
	payoff_amount decimal(19,2) null,
	return_amount decimal(19,2) null,
	wager_amount decimal(19,2) null,
	player_id bigint not null
);
